package com.bagoye.board.repository;

import com.bagoye.board.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

    RefreshTokenEntity findByTokenHash(String tokenHash);

    @Modifying
    @Query(
            value = "DELETE FROM refresh_token WHERE token_hash = ?1",
            nativeQuery = true
    )
    int deleteByTokenHash(String tokenHash);
}
