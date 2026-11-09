package com.bagoye.board.repository;

import com.bagoye.board.common.SecurityEventType;
import com.bagoye.board.entity.SecurityEventEntity;
import com.bagoye.board.repository.resultSet.GetSecurityEventDailyCountResultSet;
import com.bagoye.board.repository.resultSet.GetSecurityEventIpCountResultSet;
import com.bagoye.board.repository.resultSet.GetSecurityEventTypeCountResultSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEventEntity, Long> {

    @Query(
            "SELECT E FROM security_event E " +
            "WHERE E.createdAt >= :from AND E.createdAt < :to " +
            "AND (:eventType IS NULL OR E.eventType = :eventType)"
    )
    Page<SecurityEventEntity> findEventList(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("eventType") SecurityEventType eventType,
            Pageable pageable
    );

    @Query(
            value =
                    "SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS eventDate, event_type AS eventType, COUNT(*) AS count " +
                    "FROM security_event " +
                    "WHERE created_at >= ?1 AND created_at < ?2 " +
                    "GROUP BY eventDate, event_type " +
                    "ORDER BY eventDate, event_type",
            nativeQuery = true
    )
    List<GetSecurityEventDailyCountResultSet> getDailyCountList(LocalDateTime from, LocalDateTime to);

    @Query(
            value =
                    "SELECT event_type AS eventType, COUNT(*) AS count " +
                    "FROM security_event " +
                    "WHERE created_at >= ?1 AND created_at < ?2 " +
                    "GROUP BY event_type " +
                    "ORDER BY count DESC",
            nativeQuery = true
    )
    List<GetSecurityEventTypeCountResultSet> getTypeCountList(LocalDateTime from, LocalDateTime to);

    @Query(
            value =
                    "SELECT ip_address AS ipAddress, COUNT(*) AS count " +
                    "FROM security_event " +
                    "WHERE created_at >= ?1 AND created_at < ?2 " +
                    "AND ip_address IS NOT NULL " +
                    "GROUP BY ip_address " +
                    "ORDER BY count DESC " +
                    "LIMIT 10",
            nativeQuery = true
    )
    List<GetSecurityEventIpCountResultSet> getTopIpList(LocalDateTime from, LocalDateTime to);
}
