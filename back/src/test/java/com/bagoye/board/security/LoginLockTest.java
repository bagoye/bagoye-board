package com.bagoye.board.security;

import com.bagoye.board.entity.UserEntity;
import com.bagoye.board.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginLockTest {

    private static final String PASSWORD = "correct-pass1";
    private static final String WRONG_PASSWORD = "wrong-pass1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("로그인 5회 연속 실패 시 계정이 15분 잠기고, 잠긴 동안에는 올바른 비밀번호도 거부된다")
    void lockAfterFiveFailures() throws Exception {
        String email = "lock@bagoye.com";
        signUp(email, "locknick", "01011111111");

        for (int attempt = 1; attempt <= 4; attempt++) {
            signIn(email, WRONG_PASSWORD)
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("SF"));
        }

        // 5번째 실패에서 잠금
        signIn(email, WRONG_PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AL"));

        // 잠금 중에는 올바른 비밀번호도 거부 (잠긴 상태에서 비밀번호 대조 결과를 노출하지 않음)
        signIn(email, PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AL"));

        UserEntity userEntity = userRepository.findByEmail(email);
        assertThat(userEntity.isLocked()).isTrue();
        assertThat(userEntity.getLockedUntil()).isAfter(LocalDateTime.now().plusMinutes(14));
    }

    @Test
    @DisplayName("4회 실패 후 로그인에 성공하면 실패 횟수가 초기화된다")
    void resetCountOnSuccess() throws Exception {
        String email = "reset@bagoye.com";
        signUp(email, "resetnick", "01022222222");

        for (int attempt = 1; attempt <= 4; attempt++) {
            signIn(email, WRONG_PASSWORD).andExpect(jsonPath("$.code").value("SF"));
        }

        signIn(email, PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SU"))
                .andExpect(jsonPath("$.token").isNotEmpty());

        UserEntity userEntity = userRepository.findByEmail(email);
        assertThat(userEntity.getFailedLoginCount()).isZero();
        assertThat(userEntity.isLocked()).isFalse();
    }

    private void signUp(String email, String nickname, String telNumber) throws Exception {
        String body = """
                {
                  "email": "%s",
                  "password": "%s",
                  "nickname": "%s",
                  "telNumber": "%s",
                  "address": "서울시",
                  "addressDetail": "",
                  "agreedPersonal": true
                }
                """.formatted(email, PASSWORD, nickname, telNumber);

        mockMvc.perform(post("/api/v1/auth/sign-up").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }

    private ResultActions signIn(String email, String password) throws Exception {
        String body = """
                { "email": "%s", "password": "%s" }
                """.formatted(email, password);

        return mockMvc.perform(post("/api/v1/auth/sign-in").contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
