package com.bagoye.board.service.implement;

import com.bagoye.board.common.ImageFileType;
import com.bagoye.board.service.FileService;
import jakarta.annotation.PostConstruct;
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
public class FileServiceImplement implements FileService {

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
            log.warn("[FILE] Upload rejected: disallowed extension. fileName={}", sanitizeForLog(originalFileName));
            return null;
        }

        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = inputStream.readNBytes(ImageFileType.SIGNATURE_LENGTH);
            if (!imageFileType.matches(header)) {
                log.warn("[FILE] Upload rejected: file signature does not match extension. fileName={}",
                        sanitizeForLog(originalFileName));
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
            log.warn("[FILE] Path traversal attempt blocked. fileName={}", sanitizeForLog(fileName));
            return null;
        }

        if (ImageFileType.fromFileName(fileName) == null) {
            log.warn("[FILE] Disallowed file type requested. fileName={}", sanitizeForLog(fileName));
            return null;
        }

        Path targetPath;
        try {
            targetPath = uploadDirectory.resolve(fileName).normalize();
        } catch (InvalidPathException exception) {
            log.warn("[FILE] Invalid path requested. fileName={}", sanitizeForLog(fileName));
            return null;
        }

        if (!targetPath.startsWith(uploadDirectory)) {
            log.warn("[FILE] Access outside upload directory blocked. fileName={}", sanitizeForLog(fileName));
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

    private static String sanitizeForLog(String value) {
        if (value == null) return "null";
        String sanitized = value.replaceAll("[\\r\\n\\t]", "_");
        return sanitized.length() > 100 ? sanitized.substring(0, 100) + "..." : sanitized;
    }
}
