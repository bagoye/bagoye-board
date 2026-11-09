package com.bagoye.board.dto.object;

import com.bagoye.board.entity.SecurityEventEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEventListItem {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Long id;
    private String eventType;
    private String email;
    private String ipAddress;
    private String requestUri;
    private String detail;
    private String createdAt;

    public SecurityEventListItem(SecurityEventEntity securityEventEntity) {
        this.id = securityEventEntity.getId();
        this.eventType = securityEventEntity.getEventType().name();
        this.email = securityEventEntity.getEmail();
        this.ipAddress = securityEventEntity.getIpAddress();
        this.requestUri = securityEventEntity.getRequestUri();
        this.detail = securityEventEntity.getDetail();
        this.createdAt = securityEventEntity.getCreatedAt().format(FORMATTER);
    }

    public static List<SecurityEventListItem> copyList(List<SecurityEventEntity> securityEventEntities) {
        List<SecurityEventListItem> list = new ArrayList<>();
        for (SecurityEventEntity securityEventEntity : securityEventEntities) {
            SecurityEventListItem securityEventListItem = new SecurityEventListItem(securityEventEntity);
            list.add(securityEventListItem);
        }
        return list;
    }
}
