package com.bagoye.board.service.implement;

import com.bagoye.board.dto.request.auth.SignUpRequestDto;
import com.bagoye.board.dto.response.auth.SignUpResponseDto;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    ResponseEntity<? super SignUpResponseDto> signUp(SignUpRequestDto dto);


}
