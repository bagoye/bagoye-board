package com.bagoye.board.dto.response.admin;

import com.bagoye.board.common.ResponseCode;
import com.bagoye.board.common.ResponseMessage;
import com.bagoye.board.dto.object.SecurityEventCountItem;
import com.bagoye.board.dto.object.SecurityEventDailyCountItem;
import com.bagoye.board.dto.response.ResponseDto;
import com.bagoye.board.repository.resultSet.GetSecurityEventDailyCountResultSet;
import com.bagoye.board.repository.resultSet.GetSecurityEventIpCountResultSet;
import com.bagoye.board.repository.resultSet.GetSecurityEventTypeCountResultSet;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

@Getter
public class GetSecurityEventStatsResponseDto extends ResponseDto {

    private String from;
    private String to;
    private List<SecurityEventDailyCountItem> dailyCountList;
    private List<SecurityEventCountItem> typeCountList;
    private List<SecurityEventCountItem> topIpList;

    private GetSecurityEventStatsResponseDto(
            LocalDate from, LocalDate to,
            List<GetSecurityEventDailyCountResultSet> dailyResultSets,
            List<GetSecurityEventTypeCountResultSet> typeResultSets,
            List<GetSecurityEventIpCountResultSet> ipResultSets
    ) {
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        this.from = from.toString();
        this.to = to.toString();
        this.dailyCountList = SecurityEventDailyCountItem.copyList(dailyResultSets);
        this.typeCountList = SecurityEventCountItem.copyTypeList(typeResultSets);
        this.topIpList = SecurityEventCountItem.copyIpList(ipResultSets);
    }

    public static ResponseEntity<GetSecurityEventStatsResponseDto> success(
            LocalDate from, LocalDate to,
            List<GetSecurityEventDailyCountResultSet> dailyResultSets,
            List<GetSecurityEventTypeCountResultSet> typeResultSets,
            List<GetSecurityEventIpCountResultSet> ipResultSets
    ) {
        GetSecurityEventStatsResponseDto result =
                new GetSecurityEventStatsResponseDto(from, to, dailyResultSets, typeResultSets, ipResultSets);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
