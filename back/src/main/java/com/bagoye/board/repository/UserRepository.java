package com.bagoye.board.repository;

import com.bagoye.board.entity.UserEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    boolean existsByTelNumber(String telNumber);

    UserEntity findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    UserEntity findWithLockByEmail(String email);

}
