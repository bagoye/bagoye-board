package com.bagoye.board.service;

import com.bagoye.board.dto.request.board.PostBoardRequestDto;
import com.bagoye.board.dto.response.board.GetBoardResponseDto;
import com.bagoye.board.dto.response.board.GetFavoriteListResponseDto;
import com.bagoye.board.dto.response.board.PostBoardResponseDto;
import com.bagoye.board.dto.response.board.PutFavoriteResponseDto;
import org.springframework.http.ResponseEntity;

public interface BoardService {
    ResponseEntity<? super GetBoardResponseDto> getBoard(Integer boardNumber);
    ResponseEntity<? super GetFavoriteListResponseDto> getFavoriteList(Integer boardNumber);

    ResponseEntity<? super PostBoardResponseDto> postBoard(PostBoardRequestDto dto, String email);
    ResponseEntity<? super PutFavoriteResponseDto> putFavorite(Integer boardNumber, String email);

}
