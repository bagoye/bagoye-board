package com.bagoye.board.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "refresh_token")
@Table(name = "refresh_token")
public class RefreshTokenEntity {

    @Id
    @Column(length = 64)
    private String tokenHash;
    private String userEmail;
    private LocalDateTime expiredDatetime;

    public boolean isExpired() {
        return expiredDatetime.isBefore(LocalDateTime.now());
    }
}
