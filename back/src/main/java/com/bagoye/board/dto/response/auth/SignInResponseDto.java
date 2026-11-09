package com.bagoye.board.dto.response.auth;

import com.bagoye.board.common.ResponseCode;
import com.bagoye.board.common.ResponseMessage;
import com.bagoye.board.dto.response.ResponseDto;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class SignInResponseDto extends ResponseDto {

    private String token;
    private int expirationTime;
    private String refreshToken;
    private int refreshExpirationTime;

    private SignInResponseDto(String token, int expirationTime, String refreshToken, int refreshExpirationTime) {
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        this.token = token;
        this.expirationTime = expirationTime;
        this.refreshToken = refreshToken;
        this.refreshExpirationTime = refreshExpirationTime;
    }

    public static ResponseEntity<SignInResponseDto> success(String token, int expirationTime, String refreshToken, int refreshExpirationTime) {
        SignInResponseDto result = new SignInResponseDto(token, expirationTime, refreshToken, refreshExpirationTime);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<ResponseDto> signInFailed() {
        ResponseDto result = new ResponseDto(ResponseCode.SIGN_IN_FAIL, ResponseMessage.SIGN_IN_FAIL);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }
}
