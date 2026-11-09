package com.bagoye.board.security;

import com.bagoye.board.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileSecurityTest {

    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D
    };

    // base/            ← 업로드 폴더 "밖" (secret.png 위치)
    // base/upload/     ← 업로드 폴더
    private static final Path BASE_DIR;
    private static final Path UPLOAD_DIR;

    static {
        try {
            BASE_DIR = Files.createTempDirectory("bagoye-file-test");
            UPLOAD_DIR = Files.createDirectories(BASE_DIR.resolve("upload"));
            Files.writeString(BASE_DIR.resolve("secret.png"), "TOP-SECRET");
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @DynamicPropertySource
    static void fileProperties(DynamicPropertyRegistry registry) {
        registry.add("file.path", () -> UPLOAD_DIR.toString() + File.separator);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileService fileService;

    @BeforeEach
    void cleanUploadDir() throws IOException {
        try (Stream<Path> files = Files.list(UPLOAD_DIR)) {
            for (Path file : files.toList()) Files.delete(file);
        }
    }

    @Test
    @DisplayName("허용되지 않은 확장자(.jsp) 업로드는 거부되고 파일이 저장되지 않는다")
    void rejectDisallowedExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "shell.jsp", "application/octet-stream",
                "<% Runtime.getRuntime().exec(request.getParameter(\"cmd\")); %>".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/file/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        assertThat(uploadedFileCount()).isZero();
    }

    @Test
    @DisplayName("확장자는 .png지만 내용이 텍스트인 위조 파일은 시그니처 검사에서 거부된다")
    void rejectSpoofedImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "fake.png", "image/png",
                "this is plain text, not an image".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/file/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        assertThat(uploadedFileCount()).isZero();
    }

    @Test
    @DisplayName("[대조군] 실제 PNG 시그니처를 가진 파일은 업로드된다")
    void acceptRealPng() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.PNG", "image/png", PNG_SIGNATURE);

        MvcResult result = mockMvc.perform(multipart("/file/upload").file(file))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).endsWith(".png");
        assertThat(uploadedFileCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("../ 가 포함된 파일명은 서비스 단에서 거부되어 업로드 폴더 밖 파일을 읽을 수 없다")
    void rejectPathTraversalInService() throws IOException {
        assertThat(Files.exists(UPLOAD_DIR.resolve("../secret.png").normalize())).isTrue();

        assertThat(fileService.getImage("../secret.png")).isNull();
        assertThat(fileService.getImage("..\\secret.png")).isNull();
        assertThat(fileService.getImage("../../secret.png")).isNull();
        assertThat(fileService.getImage("upload/../../secret.png")).isNull();

        // [대조군] 업로드 폴더 안의 정상 파일은 조회된다
        Files.write(UPLOAD_DIR.resolve("normal.png"), PNG_SIGNATURE);
        assertThat(fileService.getImage("normal.png")).isNotNull();
    }

    @Test
    @DisplayName("../ 가 포함된 파일명으로 HTTP 조회 시 4xx로 거부되고 비밀 파일 내용이 노출되지 않는다")
    void rejectPathTraversalOverHttp() throws Exception {
        MvcResult result = mockMvc.perform(get("/file/{fileName}", "../secret.png"))
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).doesNotContain("TOP-SECRET");
    }

    private long uploadedFileCount() throws IOException {
        try (Stream<Path> files = Files.list(UPLOAD_DIR)) {
            return files.count();
        }
    }
}
