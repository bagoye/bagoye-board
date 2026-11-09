package com.bagoye.board.service.implement;

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

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


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
    public ResponseEntity<? super SignInResponseDto> signIn(SignInRequestDto dto) {

        String accessToken = null;
        String refreshToken = null;

        try {

            String email = dto.getEmail();
            UserEntity userEntity = userRepository.findByEmail(email);
            if (userEntity == null) return SignInResponseDto.signInFailed();

            String password = dto.getPassword();
            String encodedPassword = userEntity.getPassword();
            boolean isMatched = passwordEncoder.matches(password, encodedPassword);
            if(!isMatched) return SignInResponseDto.signInFailed();

            accessToken = jwtProvider.create(email);
            refreshToken = issueRefreshToken(email);

        } catch (Exception exception) {
            exception.printStackTrace();
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
                log.warn("[AUTH] Unknown or already used refresh token.");
                return RefreshResponseDto.refreshFailed();
            }

            // 1회용: 먼저 삭제하고, 삭제된 행이 없으면 동시 재사용으로 간주
            int deleted = refreshTokenRepository.deleteByTokenHash(tokenHash);
            if (deleted == 0) {
                log.warn("[AUTH] Concurrent refresh token reuse detected. user={}", refreshTokenEntity.getUserEmail());
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
