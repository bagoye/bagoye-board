package com.bagoye.board.service.implement;

import com.bagoye.board.dto.response.ResponseDto;
import com.bagoye.board.dto.response.search.GetPopularListResponseDto;
import com.bagoye.board.dto.response.search.GetRelationListResponseDto;
import com.bagoye.board.repository.SearchLogRepository;
import com.bagoye.board.repository.resultSet.GetPopularListResultSet;
import com.bagoye.board.repository.resultSet.GetRelationListResultSet;
import com.bagoye.board.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImplement implements SearchService {

    private final SearchLogRepository searchLogRepository;

    @Override
    public ResponseEntity<? super GetPopularListResponseDto> getPopularList() {

        List<GetPopularListResultSet> resultSets = new ArrayList<>();

        try {

            resultSets = searchLogRepository.getPopularList();

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }

        return GetPopularListResponseDto.success(resultSets);
    }

    @Override
    public ResponseEntity<? super GetRelationListResponseDto> getRelationList(String searchWord) {

        List<GetRelationListResultSet> resultSets = new ArrayList<>();

        try {

            resultSets = searchLogRepository.getRelationList(searchWord);

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
        return GetRelationListResponseDto.success(resultSets);
    }
}
