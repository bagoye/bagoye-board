package com.bagoye.board.security;

import com.bagoye.board.provider.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminAccessTest {

    private static final String USER_EMAIL = "user@bagoye.com";
    private static final String ADMIN_EMAIL = "admin@bagoye.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws Exception {
        signUpIfAbsent(USER_EMAIL, "usernick", "01033333333");
        signUpIfAbsent(ADMIN_EMAIL, "adminnick", "01044444444");
        jdbcTemplate.update("UPDATE user SET role = 'ADMIN' WHERE email = ?", ADMIN_EMAIL);
    }

    @Test
    @DisplayName("USER 토큰으로 관리자 API에 접근하면 403(NP)으로 거부된다")
    void userTokenIsForbidden() throws Exception {
        String userToken = jwtProvider.create(USER_EMAIL);

        mockMvc.perform(get("/api/v1/admin/security-events").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NP"));

        mockMvc.perform(get("/api/v1/admin/security-events/stats").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NP"));
    }

    @Test
    @DisplayName("[대조군] 토큰 없이 접근하면 401, ADMIN 토큰이면 200")
    void noTokenIsUnauthorizedAndAdminIsAllowed() throws Exception {
        mockMvc.perform(get("/api/v1/admin/security-events"))
                .andExpect(status().isUnauthorized());

        String adminToken = jwtProvider.create(ADMIN_EMAIL);
        mockMvc.perform(get("/api/v1/admin/security-events").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SU"));
    }

    private void signUpIfAbsent(String email, String nickname, String telNumber) throws Exception {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user WHERE email = ?", Integer.class, email);
        if (count != null && count > 0) return;

        String body = """
                {
                  "email": "%s",
                  "password": "password123",
                  "nickname": "%s",
                  "telNumber": "%s",
                  "address": "서울시",
                  "addressDetail": "",
                  "agreedPersonal": true
                }
                """.formatted(email, nickname, telNumber);

        mockMvc.perform(post("/api/v1/auth/sign-up").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }
}
