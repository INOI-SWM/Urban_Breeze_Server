package com.ridingmate.api_server.domain.activity.repository;

import com.ridingmate.api_server.domain.activity.dto.projection.ActivityDateRangeProjection;
import com.ridingmate.api_server.domain.activity.dto.projection.ActivityStatsProjection;
import com.ridingmate.api_server.domain.activity.dto.projection.MonthlyStatsProjection;
import com.ridingmate.api_server.domain.activity.dto.projection.YearlyStatsProjection;
import com.ridingmate.api_server.domain.activity.entity.Activity;
import com.ridingmate.api_server.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    /**
     * 사용자별 활동 목록을 조회 (User 정보 함께 fetch, 정렬은 Pageable로 처리)
     * @param user 사용자
     * @param pageable 페이지 정보 (정렬 포함)
     * @return 활동 페이지
     */
    @Query("SELECT a FROM Activity a " +
           "JOIN FETCH a.user " +
           "WHERE a.user = :user AND a.isDelete = false")
    Page<Activity> findByUserWithSort(@Param("user") User user, Pageable pageable);

    /**
     * 특정 활동과 연관된 사용자 정보를 함께 조회
     * @param activityId 활동 ID
     * @return Activity with User
     */
    @Query("SELECT a FROM Activity a " +
           "JOIN FETCH a.user " +
           "WHERE a.id = :activityId AND a.isDelete = false")
    Activity findActivityWithUser(@Param("activityId") Long activityId);

    /**
     * activityId로 활동 조회 (User 정보 함께 fetch)
     * @param activityId UUID 기반 활동 ID
     * @return Activity with User
     */
    @Query("SELECT a FROM Activity a " +
           "JOIN FETCH a.user " +
           "WHERE a.activityId = :activityId AND a.isDelete = false")
    Optional<Activity> findByActivityId(@Param("activityId") UUID activityId);

    /**
     * 사용자의 첫 번째와 마지막 활동 날짜 조회
     */
    @Query("""
        SELECT new com.ridingmate.api_server.domain.activity.dto.projection.ActivityDateRangeProjection(
            MIN(a.startedAt), 
            MAX(a.startedAt)
        ) 
        FROM Activity a 
        WHERE a.user = :user AND a.isDelete = false
        """)
    ActivityDateRangeProjection findFirstAndLastActivityDate(@Param("user") User user);

    /**
     * 특정 기간 동안의 활동 통계 조회
     * Native Query 사용으로 Duration 집계 처리 (나노초 → 초 단위 변환)
     */
    @Query(value = """
        SELECT 
            CAST(COUNT(*) AS BIGINT) as count,
            CAST(COALESCE(SUM(distance), 0.0) AS DOUBLE PRECISION) as totalDistance,
            CAST(COALESCE(SUM(elevation_gain), 0.0) AS DOUBLE PRECISION) as totalElevation,
            CAST(ROUND(COALESCE(SUM(duration), 0.0) / 1000000000.0) AS BIGINT) as totalDurationSeconds
        FROM activities 
        WHERE user_id = :userId 
        AND started_at >= :startDate 
        AND started_at < :endDate
        AND is_delete = false
        """, nativeQuery = true)
    ActivityStatsProjection findActivityStatsByPeriod(@Param("userId") Long userId,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    /**
     * 전체 활동 통계 조회
     * Native Query 사용으로 Duration 집계 처리 (나노초 → 초 단위 변환)
     */
    @Query(value = """
        SELECT 
            CAST(COUNT(*) AS BIGINT) as count,
            CAST(COALESCE(SUM(distance), 0.0) AS DOUBLE PRECISION) as totalDistance,
            CAST(COALESCE(SUM(elevation_gain), 0.0) AS DOUBLE PRECISION) as totalElevation,
            CAST(ROUND(COALESCE(SUM(duration), 0.0) / 1000000000.0) AS BIGINT) as totalDurationSeconds
        FROM activities 
        WHERE user_id = :userId AND is_delete = false
        """, nativeQuery = true)
    ActivityStatsProjection findOverallActivityStats(@Param("userId") Long userId);

    /**
     * 연별 활동 통계 조회
     */
    @Query(value = """
        SELECT 
            EXTRACT(YEAR FROM started_at) as year,
            CAST(COUNT(*) AS BIGINT) as count,
            CAST(COALESCE(SUM(distance), 0.0) AS DOUBLE PRECISION) as totalDistance,
            CAST(COALESCE(SUM(elevation_gain), 0.0) AS DOUBLE PRECISION) as totalElevation,
            CAST(ROUND(COALESCE(SUM(duration), 0.0) / 1000000000.0) AS BIGINT) as totalDurationSeconds
        FROM activities 
        WHERE user_id = :userId AND is_delete = false
        GROUP BY EXTRACT(YEAR FROM started_at)
        ORDER BY year DESC
        """, nativeQuery = true)
    List<YearlyStatsProjection> findYearlyActivityStats(@Param("userId") Long userId);

    /**
     * 월별 활동 통계 조회 (특정 연도)
     */
    @Query(value = """
        SELECT 
            EXTRACT(MONTH FROM started_at) as month,
            CAST(COUNT(*) AS BIGINT) as count,
            CAST(COALESCE(SUM(distance), 0.0) AS DOUBLE PRECISION) as totalDistance,
            CAST(COALESCE(SUM(elevation_gain), 0.0) AS DOUBLE PRECISION) as totalElevation,
            CAST(ROUND(COALESCE(SUM(duration), 0.0) / 1000000000.0) AS BIGINT) as totalDurationSeconds
        FROM activities 
        WHERE user_id = :userId 
        AND EXTRACT(YEAR FROM started_at) = :year 
        AND is_delete = false
        GROUP BY EXTRACT(MONTH FROM started_at)
        ORDER BY month ASC
        """, nativeQuery = true)
    List<MonthlyStatsProjection> findMonthlyActivityStats(@Param("userId") Long userId, @Param("year") Integer year);

    /**
     * 특정 사용자의 모든 활동 조회 (삭제된 활동 포함)
     */
    List<Activity> findByUser(User user);

    /**
     * 특정 사용자의 활성 활동 조회 (삭제되지 않은 활동만)
     */
    List<Activity> findByUserAndIsDeleteFalse(User user);

    /**
     * 사용자의 가장 오래된 활동 날짜 조회
     */
    @Query("SELECT MIN(a.startedAt) FROM Activity a WHERE a.user = :user AND a.isDelete = false")
    LocalDateTime findOldestActivityDate(@Param("user") User user);

}