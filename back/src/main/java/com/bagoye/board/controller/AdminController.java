package com.bagoye.board.controller;

import com.bagoye.board.common.SecurityEventType;
import com.bagoye.board.dto.response.admin.GetSecurityEventListResponseDto;
import com.bagoye.board.dto.response.admin.GetSecurityEventStatsResponseDto;
import com.bagoye.board.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/security-events")
    public ResponseEntity<? super GetSecurityEventListResponseDto> getSecurityEventList(
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "type", required = false) SecurityEventType type,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        ResponseEntity<? super GetSecurityEventListResponseDto> response =
                adminService.getSecurityEventList(from, to, type, page, size);
        return response;
    }

    @GetMapping("/security-events/stats")
    public ResponseEntity<? super GetSecurityEventStatsResponseDto> getSecurityEventStats(
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        ResponseEntity<? super GetSecurityEventStatsResponseDto> response =
                adminService.getSecurityEventStats(from, to);
        return response;
    }
}
