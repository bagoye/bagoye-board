package com.bagoye.board.dto.response.admin;

import com.bagoye.board.common.ResponseCode;
import com.bagoye.board.common.ResponseMessage;
import com.bagoye.board.dto.object.SecurityEventListItem;
import com.bagoye.board.dto.response.ResponseDto;
import com.bagoye.board.entity.SecurityEventEntity;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Getter
public class GetSecurityEventListResponseDto extends ResponseDto {

    private List<SecurityEventListItem> securityEventList;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    private GetSecurityEventListResponseDto(Page<SecurityEventEntity> eventPage) {
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        this.securityEventList = SecurityEventListItem.copyList(eventPage.getContent());
        this.page = eventPage.getNumber();
        this.size = eventPage.getSize();
        this.totalElements = eventPage.getTotalElements();
        this.totalPages = eventPage.getTotalPages();
    }

    public static ResponseEntity<GetSecurityEventListResponseDto> success(Page<SecurityEventEntity> eventPage) {
        GetSecurityEventListResponseDto result = new GetSecurityEventListResponseDto(eventPage);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
