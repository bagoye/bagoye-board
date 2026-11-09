package com.bagoye.board.service.implement;

import com.bagoye.board.audit.SecurityEventRecorder;
import com.bagoye.board.common.SecurityEventType;
import com.bagoye.board.dto.request.auth.RefreshRequestDto;
import com.bagoye.board.dto.request.auth.SignInRequestDto;
import com.bagoye.board.dto.request.auth.SignUpRequestDto;
import com.bagoye.board.dto.response.ResponseDto;
import com.bagoye.board.dto.response.auth.RefreshResponseDto;
import com.bagoye.board.dto.response.auth.SignInResponseDto;
import com.bagoye.board.dto.response.auth.SignUpResponseDto;
import com.bagoye.board.entity.RefreshTokenEntity;
import com.bagoye.board.entity.UserEntity;
import com.bagoye.board.provider.JwtProvider;
import com.bagoye.board.provider.RefreshTokenProvider;
import com.bagoye.board.repository.RefreshTokenRepository;
import com.bagoye.board.repository.UserRepository;
import com.bagoye.board.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImplement implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenProvider refreshTokenProvider;
    private final SecurityEventRecorder securityEventRecorder;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final int MAX_FAILED_LOGIN_COUNT = 5;
    private static final long LOCK_MINUTES = 15;

    // 존재하지 않는 이메일에도 BCrypt 비교를 수행해 응답 시간 차이로 계정이 드러나지 않게 함
    private final String dummyPasswordHash = passwordEncoder.encode("dummy-password-for-timing-equalization");


    @Override
    public ResponseEntity<? super SignUpResponseDto> signUp(SignUpRequestDto dto) {

        try {

            String email = dto.getEmail();
            boolean existedEmail = userRepository.existsByEmail(email);
            if (existedEmail) return SignUpResponseDto.duplicateEmail();

            String nickname = dto.getNickname();
            boolean existedNickname = userRepository.existsByNickname(nickname);
            if (existedNickname) return SignUpResponseDto.duplicateNickname();

            String telNumber = dto.getTelNumber();
            boolean existedTelNumber = userRepository.existsByTelNumber(telNumber);
            if (existedTelNumber) return SignUpResponseDto.duplicateTelNumber();

            String password = dto.getPassword();
            String encodedPassword = passwordEncoder.encode(password);
            dto.setPassword(encodedPassword);

            UserEntity userEntity = new UserEntity(dto);
            userRepository.save(userEntity);

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }

        return SignUpResponseDto.success();
    }

    @Override
    @Transactional
    public ResponseEntity<? super SignInResponseDto> signIn(SignInRequestDto dto) {

        String accessToken = null;
        String refreshToken = null;

        try {

            String email = dto.getEmail();
            String password = dto.getPassword();

            UserEntity userEntity = userRepository.findWithLockByEmail(email);
            if (userEntity == null) {
                passwordEncoder.matches(password, dummyPasswordHash);
                securityEventRecorder.record(SecurityEventType.LOGIN_FAILED, email, "unknown-email");
                return SignInResponseDto.signInFailed();
            }

            if (userEntity.isLocked()) {
                securityEventRecorder.record(SecurityEventType.ACCOUNT_LOCKED, email,
                        "attempt-while-locked, lockedUntil=" + userEntity.getLockedUntil());
                return SignInResponseDto.accountLocked();
            }

            String encodedPassword = userEntity.getPassword();
            boolean isMatched = passwordEncoder.matches(password, encodedPassword);
            if (!isMatched) {
                userEntity.increaseFailedLoginCount(MAX_FAILED_LOGIN_COUNT, LOCK_MINUTES);
                userRepository.save(userEntity);

                if (userEntity.isLocked()) {
                    securityEventRecorder.record(SecurityEventType.ACCOUNT_LOCKED, email,
                            "locked-after-" + MAX_FAILED_LOGIN_COUNT + "-failures");
                    return SignInResponseDto.accountLocked();
                }

                securityEventRecorder.record(SecurityEventType.LOGIN_FAILED, email,
                        "bad-credentials, failedCount=" + userEntity.getFailedLoginCount());
                return SignInResponseDto.signInFailed();
            }

            if (userEntity.getFailedLoginCount() > 0 || userEntity.getLockedUntil() != null) {
                userEntity.resetFailedLoginCount();
                userRepository.save(userEntity);
            }

            accessToken = jwtProvider.create(email);
            refreshToken = issueRefreshToken(email);

        } catch (Exception exception) {
            log.error("[AUTH] Sign-in error.", exception);
            return ResponseDto.databaseError();
        }

        return SignInResponseDto.success(
                accessToken, jwtProvider.getAccessTokenExpirationSeconds(),
                refreshToken, refreshTokenProvider.getExpirationSeconds());
    }

    @Override
    @Transactional
    public ResponseEntity<? super RefreshResponseDto> refresh(RefreshRequestDto dto) {

        String accessToken = null;
        String refreshToken = null;

        try {

            String tokenHash = refreshTokenProvider.hash(dto.getRefreshToken());
            RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByTokenHash(tokenHash);
            if (refreshTokenEntity == null) {
                securityEventRecorder.record(SecurityEventType.INVALID_TOKEN, null, "refresh-token-unknown-or-reused");
                return RefreshResponseDto.refreshFailed();
            }

            // 1회용: 먼저 삭제하고, 삭제된 행이 없으면 동시 재사용으로 간주
            int deleted = refreshTokenRepository.deleteByTokenHash(tokenHash);
            if (deleted == 0) {
                securityEventRecorder.record(SecurityEventType.INVALID_TOKEN, refreshTokenEntity.getUserEmail(),
                        "refresh-token-concurrent-reuse");
                return RefreshResponseDto.refreshFailed();
            }

            if (refreshTokenEntity.isExpired()) {
                log.info("[AUTH] Expired refresh token. user={}", refreshTokenEntity.getUserEmail());
                return RefreshResponseDto.refreshFailed();
            }

            String email = refreshTokenEntity.getUserEmail();
            boolean existedUser = userRepository.existsByEmail(email);
            if (!existedUser) return RefreshResponseDto.refreshFailed();

            accessToken = jwtProvider.create(email);
            refreshToken = issueRefreshToken(email);

        } catch (Exception exception) {
            log.error("[AUTH] Refresh failed.", exception);
            return ResponseDto.databaseError();
        }

        return RefreshResponseDto.success(
                accessToken, jwtProvider.getAccessTokenExpirationSeconds(),
                refreshToken, refreshTokenProvider.getExpirationSeconds());
    }

    private String issueRefreshToken(String email) {
        String refreshToken = refreshTokenProvider.create();
        String tokenHash = refreshTokenProvider.hash(refreshToken);

        RefreshTokenEntity refreshTokenEntity =
                new RefreshTokenEntity(tokenHash, email, refreshTokenProvider.getExpiredDatetime());
        refreshTokenRepository.save(refreshTokenEntity);

        return refreshToken;
    }
}
