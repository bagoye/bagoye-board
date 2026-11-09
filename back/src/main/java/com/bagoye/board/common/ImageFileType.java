package com.bagoye.board.common;

import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

public enum ImageFileType {

    JPEG(Set.of("jpg", "jpeg"), MediaType.IMAGE_JPEG) {
        @Override
        public boolean matches(byte[] header) {
            return startsWith(header, 0, 0xFF, 0xD8, 0xFF);
        }
    },
    PNG(Set.of("png"), MediaType.IMAGE_PNG) {
        @Override
        public boolean matches(byte[] header) {
            return startsWith(header, 0, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
        }
    },
    GIF(Set.of("gif"), MediaType.IMAGE_GIF) {
        @Override
        public boolean matches(byte[] header) {
            return startsWith(header, 0, 0x47, 0x49, 0x46, 0x38, 0x37, 0x61)   // GIF87a
                    || startsWith(header, 0, 0x47, 0x49, 0x46, 0x38, 0x39, 0x61); // GIF89a
        }
    },
    WEBP(Set.of("webp"), new MediaType("image", "webp")) {
        @Override
        public boolean matches(byte[] header) {
            return startsWith(header, 0, 0x52, 0x49, 0x46, 0x46)    // RIFF
                    && startsWith(header, 8, 0x57, 0x45, 0x42, 0x50); // WEBP
        }
    };

    public static final int SIGNATURE_LENGTH = 12;

    private final Set<String> extensions;
    private final MediaType mediaType;

    ImageFileType(Set<String> extensions, MediaType mediaType) {
        this.extensions = extensions;
        this.mediaType = mediaType;
    }

    public abstract boolean matches(byte[] header);

    public MediaType getMediaType() {
        return mediaType;
    }

    public static ImageFileType fromFileName(String fileName) {
        String extension = extractExtension(fileName);
        if (extension == null) return null;

        return Arrays.stream(values())
                .filter(type -> type.extensions.contains(extension))
                .findFirst()
                .orElse(null);
    }

    public static String extractExtension(String fileName) {
        if (fileName == null) return null;

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex == fileName.length() - 1) return null;

        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private static boolean startsWith(byte[] data, int offset, int... signature) {
        if (data == null || data.length < offset + signature.length) return false;

        for (int index = 0; index < signature.length; index++) {
            if ((data[offset + index] & 0xFF) != signature[index]) return false;
        }
        return true;
    }
}
