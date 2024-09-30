package com.bagoye.board.service;

import com.bagoye.board.dto.request.board.PatchBoardRequestDto;
import com.bagoye.board.dto.request.user.PatchNicknameRequestDto;
import com.bagoye.board.dto.request.user.PatchProfileImageRequestDto;
import com.bagoye.board.dto.response.user.GetSignInUserResponseDto;
import com.bagoye.board.dto.response.user.GetUserResponseDto;
import com.bagoye.board.dto.response.user.PatchNicknameResponseDto;
import com.bagoye.board.dto.response.user.PatchProfileImageResponseDto;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<? super GetUserResponseDto> getUser(String email);
    ResponseEntity<? super GetSignInUserResponseDto> getSignInUser(String email);
    ResponseEntity<? super PatchNicknameResponseDto> patchNickname(PatchNicknameRequestDto dto, String email);
    ResponseEntity<? super PatchProfileImageResponseDto> patchProfileImage(PatchProfileImageRequestDto dto, String email);
}
