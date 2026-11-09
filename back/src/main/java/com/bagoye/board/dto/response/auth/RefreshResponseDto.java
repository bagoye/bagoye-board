package com.bagoye.board.dto.response.auth;

import com.bagoye.board.common.ResponseCode;
import com.bagoye.board.common.ResponseMessage;
import com.bagoye.board.dto.response.ResponseDto;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class RefreshResponseDto extends ResponseDto {

    private String token;
    private int expirationTime;
    private String refreshToken;
    private int refreshExpirationTime;

    private RefreshResponseDto(String token, int expirationTime, String refreshToken, int refreshExpirationTime) {
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        this.token = token;
        this.expirationTime = expirationTime;
        this.refreshToken = refreshToken;
        this.refreshExpirationTime = refreshExpirationTime;
    }

    public static ResponseEntity<RefreshResponseDto> success(String token, int expirationTime, String refreshToken, int refreshExpirationTime) {
        RefreshResponseDto result = new RefreshResponseDto(token, expirationTime, refreshToken, refreshExpirationTime);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<ResponseDto> refreshFailed() {
        ResponseDto result = new ResponseDto(ResponseCode.AUTHORIZATION_FAIL, ResponseMessage.AUTHORIZATION_FAIL);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }
}
