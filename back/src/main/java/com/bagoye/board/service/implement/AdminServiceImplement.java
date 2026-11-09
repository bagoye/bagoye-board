package com.bagoye.board.service.implement;

import com.bagoye.board.common.SecurityEventType;
import com.bagoye.board.dto.response.ResponseDto;
import com.bagoye.board.dto.response.admin.GetSecurityEventListResponseDto;
import com.bagoye.board.dto.response.admin.GetSecurityEventStatsResponseDto;
import com.bagoye.board.entity.SecurityEventEntity;
import com.bagoye.board.repository.SecurityEventRepository;
import com.bagoye.board.repository.resultSet.GetSecurityEventDailyCountResultSet;
import com.bagoye.board.repository.resultSet.GetSecurityEventIpCountResultSet;
import com.bagoye.board.repository.resultSet.GetSecurityEventTypeCountResultSet;
import com.bagoye.board.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImplement implements AdminService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final long DEFAULT_RANGE_DAYS = 7;
    private static final long MAX_RANGE_DAYS = 90;

    private final SecurityEventRepository securityEventRepository;

    @Override
    public ResponseEntity<? super GetSecurityEventListResponseDto> getSecurityEventList(
            LocalDate from, LocalDate to, SecurityEventType eventType, int page, int size) {

        LocalDate toDate = to == null ? LocalDate.now() : to;
        LocalDate fromDate = from == null ? toDate.minusDays(DEFAULT_RANGE_DAYS - 1) : from;

        if (!isValidRange(fromDate, toDate) || page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            return ResponseDto.validationFailed();
        }

        Page<SecurityEventEntity> eventPage = null;

        try {

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            eventPage = securityEventRepository.findEventList(
                    toStartOfDay(fromDate), toStartOfDay(toDate.plusDays(1)), eventType, pageable);

        } catch (Exception exception) {
            log.error("[ADMIN] Failed to get security event list.", exception);
            return ResponseDto.databaseError();
        }

        return GetSecurityEventListResponseDto.success(eventPage);
    }

    @Override
    public ResponseEntity<? super GetSecurityEventStatsResponseDto> getSecurityEventStats(LocalDate from, LocalDate to) {

        LocalDate toDate = to == null ? LocalDate.now() : to;
        LocalDate fromDate = from == null ? toDate.minusDays(DEFAULT_RANGE_DAYS - 1) : from;

        if (!isValidRange(fromDate, toDate)) return ResponseDto.validationFailed();

        List<GetSecurityEventDailyCountResultSet> dailyResultSets = null;
        List<GetSecurityEventTypeCountResultSet> typeResultSets = null;
        List<GetSecurityEventIpCountResultSet> ipResultSets = null;

        try {

            LocalDateTime start = toStartOfDay(fromDate);
            LocalDateTime end = toStartOfDay(toDate.plusDays(1));

            dailyResultSets = securityEventRepository.getDailyCountList(start, end);
            typeResultSets = securityEventRepository.getTypeCountList(start, end);
            ipResultSets = securityEventRepository.getTopIpList(start, end);

        } catch (Exception exception) {
            log.error("[ADMIN] Failed to get security event stats.", exception);
            return ResponseDto.databaseError();
        }

        return GetSecurityEventStatsResponseDto.success(fromDate, toDate, dailyResultSets, typeResultSets, ipResultSets);
    }

    private boolean isValidRange(LocalDate fromDate, LocalDate toDate) {
        return !fromDate.isAfter(toDate) && ChronoUnit.DAYS.between(fromDate, toDate) < MAX_RANGE_DAYS;
    }

    private LocalDateTime toStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }
}
