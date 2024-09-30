package com.bagoye.board.service;

import com.bagoye.board.dto.response.search.GetPopularListResponseDto;
import com.bagoye.board.dto.response.search.GetRelationListResponseDto;
import org.springframework.http.ResponseEntity;

public interface SearchService {

    ResponseEntity<? super GetPopularListResponseDto> getPopularList();
    ResponseEntity<? super GetRelationListResponseDto> getRelationList(String searchWord);
}
