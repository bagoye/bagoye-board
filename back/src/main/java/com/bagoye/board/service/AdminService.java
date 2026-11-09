package com.bagoye.board.service;

import com.bagoye.board.common.SecurityEventType;
import com.bagoye.board.dto.response.admin.GetSecurityEventListResponseDto;
import com.bagoye.board.dto.response.admin.GetSecurityEventStatsResponseDto;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public interface AdminService {

    ResponseEntity<? super GetSecurityEventListResponseDto> getSecurityEventList(
            LocalDate from, LocalDate to, SecurityEventType eventType, int page, int size);

    ResponseEntity<? super GetSecurityEventStatsResponseDto> getSecurityEventStats(LocalDate from, LocalDate to);

}
