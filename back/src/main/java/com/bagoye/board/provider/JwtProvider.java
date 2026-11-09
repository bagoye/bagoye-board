package com.bagoye.board.provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private static final int MIN_SECRET_KEY_BYTES = 32; // HS256 = 256bit

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-token-expiration-minutes:30}")
    private long accessTokenExpirationMinutes;

    private Key key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_KEY_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret-key must be at least " + MIN_SECRET_KEY_BYTES + " bytes (current: " + keyBytes.length + " bytes).");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String create(String email) {
        Date expiredDate = Date.from(Instant.now().plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES));

        String jwt = Jwts.builder()
                .signWith(key, SignatureAlgorithm.HS256)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(expiredDate)
                .compact();

        return jwt;
    }

    public int getAccessTokenExpirationSeconds() {
        return (int) (accessTokenExpirationMinutes * 60);
    }

    public String validate(String jwt) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
            return claims.getSubject();
        } catch (ExpiredJwtException exception) {
            log.info("[JWT] Expired token.");
        } catch (io.jsonwebtoken.security.SecurityException exception) {
            log.warn("[JWT] Invalid signature. Possible token forgery attempt.");
        } catch (MalformedJwtException exception) {
            log.warn("[JWT] Malformed token.");
        } catch (UnsupportedJwtException exception) {
            log.warn("[JWT] Unsupported token.");
        } catch (IllegalArgumentException exception) {
            log.warn("[JWT] Empty token.");
        } catch (JwtException exception) {
            log.warn("[JWT] Invalid token: {}", exception.getClass().getSimpleName());
        }
        return null;
    }
}
