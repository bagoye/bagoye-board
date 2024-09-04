package com.bagoye.board.repository;

import com.bagoye.board.entity.FavoriteEntity;
import com.bagoye.board.entity.primaryKey.FavoritePk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<FavoriteEntity, FavoritePk> {

}
