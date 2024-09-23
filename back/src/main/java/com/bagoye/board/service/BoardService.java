package com.bagoye.board.service;

import com.bagoye.board.dto.request.board.PostBoardRequestDto;
import com.bagoye.board.dto.response.board.GetBoardResponseDto;
import com.bagoye.board.dto.response.board.PostBoardResponseDto;
import org.springframework.http.ResponseEntity;

public interface BoardService {
    ResponseEntity<? super GetBoardResponseDto> getBoard(Integer boardNumber);
    ResponseEntity<? super PostBoardResponseDto> postBoard(PostBoardRequestDto dto, String email);
}
