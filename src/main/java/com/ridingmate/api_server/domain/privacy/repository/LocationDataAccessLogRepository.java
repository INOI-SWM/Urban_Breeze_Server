package com.ridingmate.api_server.domain.privacy.repository;

import com.ridingmate.api_server.domain.privacy.entity.LocationDataAccessLog;
import com.ridingmate.api_server.domain.privacy.enums.LocationAccessType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LocationDataAccessLogRepository extends JpaRepository<LocationDataAccessLog, Long> {

    /**
     * 특정 사용자의 위치정보 조회 기록 조회
     */
    @Query("SELECT l FROM LocationDataAccessLog l WHERE l.user.id = :userId ORDER BY l.accessedAt DESC")
    Page<LocationDataAccessLog> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 특정 기간의 위치정보 조회 기록 조회
     */
    @Query("SELECT l FROM LocationDataAccessLog l WHERE l.accessedAt BETWEEN :startDate AND :endDate ORDER BY l.accessedAt DESC")
    Page<LocationDataAccessLog> findByAccessedAtBetween(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate,
                                                      Pageable pageable);

    /**
     * 만료된 기록 조회 (보존기간 만료)
     */
    @Query("SELECT l FROM LocationDataAccessLog l WHERE l.accessedAt < :expiredDate")
    List<LocationDataAccessLog> findExpiredLogs(@Param("expiredDate") LocalDateTime expiredDate);

    /**
     * 특정 데이터 타입의 조회 기록
     */
    @Query("SELECT l FROM LocationDataAccessLog l WHERE l.dataType = :dataType AND l.dataId = :dataId")
    List<LocationDataAccessLog> findByDataTypeAndDataId(@Param("dataType") String dataType,
                                                       @Param("dataId") String dataId);

    /**
     * 특정 접근 유형의 조회 기록
     */
    @Query("SELECT l FROM LocationDataAccessLog l WHERE l.accessType = :accessType ORDER BY l.accessedAt DESC")
    Page<LocationDataAccessLog> findByAccessType(@Param("accessType") LocationAccessType accessType,
                                                Pageable pageable);

    /**
     * 만료된 기록 삭제
     */
    @Query("DELETE FROM LocationDataAccessLog l WHERE l.accessedAt < :expiredDate")
    void deleteExpiredLogs(@Param("expiredDate") LocalDateTime expiredDate);
}
