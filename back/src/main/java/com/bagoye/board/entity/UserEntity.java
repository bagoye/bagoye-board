package com.bagoye.board.entity;

import com.bagoye.board.dto.request.auth.SignUpRequestDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "user")
@Table(name = "user")
public class UserEntity {

    @Id
    private String email;
    private String password;
    private String nickname;
    private String telNumber;
    private String address;
    private String addressDetail;
    private String profileImage;
    private boolean agreedPersonal;

    @Column(nullable = false, columnDefinition = "INT NOT NULL DEFAULT 0")
    private int failedLoginCount;
    private LocalDateTime lockedUntil;

    public UserEntity(SignUpRequestDto dto) {
        this.email = dto.getEmail();
        this.password = dto.getPassword();
        this.nickname = dto.getNickname();
        this.telNumber = dto.getTelNumber();
        this.address = dto.getAddress();
        this.addressDetail = dto.getAddressDetail();
        this.agreedPersonal = dto.getAgreedPersonal();
        this.failedLoginCount = 0;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    public void increaseFailedLoginCount(int maxFailedCount, long lockMinutes) {
        this.failedLoginCount++;
        if (this.failedLoginCount >= maxFailedCount) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(lockMinutes);
            this.failedLoginCount = 0;
        }
    }

    public void resetFailedLoginCount() {
        this.failedLoginCount = 0;
        this.lockedUntil = null;
    }
}
