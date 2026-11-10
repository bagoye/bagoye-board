package com.bagoye.board.security;

import com.bagoye.board.provider.JwtProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BoardAuthenticationTest {
    private static final String EMAIL = "board-auth@bagoye.com";
    private static final String BODY = """
            {"title":"Authentication regression","content":"Board content","boardImageList":[]}
            """;

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtProvider jwtProvider;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Value("${jwt.secret-key}") private String secretKey;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO user (email, nickname, role, failed_login_count, agreed_personal) VALUES (?, ?, 'USER', 0, true)",
                EMAIL, "board-auth");
    }

    @Test
    void validTokenSavesBoard() throws Exception {
        int before = boardCount();
        mockMvc.perform(post("/api/v1/board")
                        .header("Authorization", "Bearer " + jwtProvider.create(EMAIL))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SU"));
        assertThat(boardCount()).isEqualTo(before + 1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM board WHERE writer_email = ? AND title = ? AND content = ?",
                Integer.class, EMAIL, "Authentication regression", "Board content")).isEqualTo(1);
    }

    @Test
    void expiredTokenIsUnauthorizedWithoutSaving() throws Exception {
        String token = Jwts.builder().setSubject(EMAIL)
                .setExpiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
        assertUnauthorizedWithoutSaving(token);
    }

    @Test
    void invalidSignatureIsUnauthorizedWithoutSaving() throws Exception {
        String token = Jwts.builder().setSubject(EMAIL)
                .setExpiration(Date.from(Instant.now().plusSeconds(600)))
                .signWith(Keys.secretKeyFor(SignatureAlgorithm.HS256)).compact();
        assertUnauthorizedWithoutSaving(token);
    }

    @Test
    void malformedTokenIsUnauthorizedWithoutSaving() throws Exception {
        assertUnauthorizedWithoutSaving("invalid-token");
    }

    @Test
    void missingTokenIsUnauthorizedWithoutSaving() throws Exception {
        assertUnauthorizedWithoutSaving(null);
    }

    private void assertUnauthorizedWithoutSaving(String token) throws Exception {
        int before = boardCount();
        var request = post("/api/v1/board").contentType(MediaType.APPLICATION_JSON).content(BODY);
        if (token != null) request.header("Authorization", "Bearer " + token);
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("AF"))
                .andExpect(jsonPath("$['code:']").doesNotExist())
                .andExpect(jsonPath("$.message").value("Authorization Failed."));
        assertThat(boardCount()).isEqualTo(before);
    }

    private int boardCount() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM board", Integer.class);
    }
}
