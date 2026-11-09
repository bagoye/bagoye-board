package com.bagoye.board.service.implement;

import com.bagoye.board.audit.SecurityEventRecorder;
import com.bagoye.board.common.ImageFileType;
import com.bagoye.board.common.SecurityEventType;
import com.bagoye.board.service.FileService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImplement implements FileService {

    private final SecurityEventRecorder securityEventRecorder;

    @Value("${file.path}")
    private String filePath;

    @Value("${file.url}")
    private String fileUrl;

    private Path uploadDirectory;

    @PostConstruct
    public void init() {
        this.uploadDirectory = Paths.get(filePath).toAbsolutePath().normalize();
    }

    @Override
    public String upload(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            log.warn("[FILE] Upload rejected: empty file.");
            return null;
        }

        String originalFileName = file.getOriginalFilename();
        ImageFileType imageFileType = ImageFileType.fromFileName(originalFileName);
        if (imageFileType == null) {
            securityEventRecorder.record(SecurityEventType.UPLOAD_BLOCKED, null,
                    "disallowed-extension: " + originalFileName);
            return null;
        }

        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = inputStream.readNBytes(ImageFileType.SIGNATURE_LENGTH);
            if (!imageFileType.matches(header)) {
                securityEventRecorder.record(SecurityEventType.UPLOAD_BLOCKED, null,
                        "signature-mismatch: " + originalFileName);
                return null;
            }
        } catch (IOException exception) {
            log.error("[FILE] Failed to read uploaded file.", exception);
            return null;
        }

        String extension = ImageFileType.extractExtension(originalFileName);
        String saveFileName = UUID.randomUUID() + "." + extension;
        Path savePath = uploadDirectory.resolve(saveFileName).normalize();

        try {
            file.transferTo(savePath);
        } catch (Exception exception) {
            log.error("[FILE] Failed to save uploaded file.", exception);
            return null;
        }

        String url = fileUrl + saveFileName;
        return url;
    }

    @Override
    public Resource getImage(String fileName) {

        if (!StringUtils.hasText(fileName)
                || fileName.contains("..")
                || fileName.contains("/")
                || fileName.contains("\\")
                || fileName.indexOf('\0') >= 0) {
            securityEventRecorder.record(SecurityEventType.FILE_ACCESS_BLOCKED, null, "path-traversal: " + fileName);
            return null;
        }

        if (ImageFileType.fromFileName(fileName) == null) {
            securityEventRecorder.record(SecurityEventType.FILE_ACCESS_BLOCKED, null, "disallowed-type: " + fileName);
            return null;
        }

        Path targetPath;
        try {
            targetPath = uploadDirectory.resolve(fileName).normalize();
        } catch (InvalidPathException exception) {
            securityEventRecorder.record(SecurityEventType.FILE_ACCESS_BLOCKED, null, "invalid-path: " + fileName);
            return null;
        }

        if (!targetPath.startsWith(uploadDirectory)) {
            securityEventRecorder.record(SecurityEventType.FILE_ACCESS_BLOCKED, null, "outside-upload-dir: " + fileName);
            return null;
        }

        if (!Files.isRegularFile(targetPath) || !Files.isReadable(targetPath)) return null;

        try {
            return new UrlResource(targetPath.toUri());
        } catch (MalformedURLException exception) {
            log.error("[FILE] Failed to load file.", exception);
            return null;
        }
    }
}
