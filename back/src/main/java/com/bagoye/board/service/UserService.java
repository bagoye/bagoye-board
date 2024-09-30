package com.bagoye.board.service;

import com.bagoye.board.dto.response.user.GetSignInUserResponseDto;
import com.bagoye.board.dto.response.user.GetUserResponseDto;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<? super GetUserResponseDto> getUser(String email);
    ResponseEntity<? super GetSignInUserResponseDto> getSignInUser(String email);
}
