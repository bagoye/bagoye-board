package com.bagoye.board.entity;

import com.bagoye.board.common.SecurityEventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "security_event")
@Table(name = "security_event")
public class SecurityEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(30)")
    private SecurityEventType eventType;

    private String email;
    private String ipAddress;
    private String requestUri;
    private String detail;
    private LocalDateTime createdAt;

    public SecurityEventEntity(SecurityEventType eventType, String email, String ipAddress,
                               String requestUri, String detail) {
        this.eventType = eventType;
        this.email = email;
        this.ipAddress = ipAddress;
        this.requestUri = requestUri;
        this.detail = detail;
        this.createdAt = LocalDateTime.now();
    }
}
