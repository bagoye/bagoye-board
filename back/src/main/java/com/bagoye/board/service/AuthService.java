package com.bagoye.board.service;

import com.bagoye.board.dto.request.auth.RefreshRequestDto;
import com.bagoye.board.dto.request.auth.SignInRequestDto;
import com.bagoye.board.dto.request.auth.SignUpRequestDto;
import com.bagoye.board.dto.response.auth.RefreshResponseDto;
import com.bagoye.board.dto.response.auth.SignInResponseDto;
import com.bagoye.board.dto.response.auth.SignUpResponseDto;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    ResponseEntity<? super SignUpResponseDto> signUp(SignUpRequestDto dto);
    ResponseEntity<? super SignInResponseDto> signIn(SignInRequestDto dto);
    ResponseEntity<? super RefreshResponseDto> refresh(RefreshRequestDto dto);

}
