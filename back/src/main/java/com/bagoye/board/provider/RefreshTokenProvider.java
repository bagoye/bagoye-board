package com.bagoye.board.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class RefreshTokenProvider {

    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom secureRandom = new SecureRandom();

    @Value("${jwt.refresh-token-expiration-days:7}")
    private long expirationDays;

    public String create() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm not available.", exception);
        }
    }

    public LocalDateTime getExpiredDatetime() {
        return LocalDateTime.now().plusDays(expirationDays);
    }

    public int getExpirationSeconds() {
        return (int) (expirationDays * 24 * 60 * 60);
    }
}
