package com.ridingmate.api_server.domain.activity.repository;

import com.ridingmate.api_server.domain.activity.entity.UserApiUsage;
import com.ridingmate.api_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 사용자 API 사용량 레포지토리
 */
@Repository
public interface UserApiUsageRepository extends JpaRepository<UserApiUsage, Long> {

    /**
     * 특정 사용자의 현재 월 사용량 조회
     */
    @Query("SELECT u FROM UserApiUsage u WHERE u.user = :user AND u.year = :year AND u.month = :month")
    Optional<UserApiUsage> findByUserAndCurrentMonth(@Param("user") User user, 
                                                   @Param("year") Integer year, 
                                                   @Param("month") Integer month);

    /**
     * 특정 사용자의 현재 월 사용량 조회 (LocalDate 사용)
     */
    default Optional<UserApiUsage> findByUserAndCurrentMonth(User user) {
        LocalDate now = LocalDate.now();
        return findByUserAndCurrentMonth(user, now.getYear(), now.getMonthValue());
    }

    /**
     * 특정 사용자의 현재 월 총 사용량 조회
     */
    @Query("SELECT COALESCE(SUM(u.activitySyncCount), 0) FROM UserApiUsage u WHERE u.user = :user AND u.year = :year AND u.month = :month")
    int getTotalUsageForCurrentMonth(@Param("user") User user,
                                       @Param("year") Integer year, 
                                       @Param("month") Integer month);

    /**
     * 특정 사용자의 현재 월 총 사용량 조회 (LocalDate 사용)
     */
    default int getTotalUsageForCurrentMonth(User user) {
        LocalDate now = LocalDate.now();
        return getTotalUsageForCurrentMonth(user, now.getYear(), now.getMonthValue());
    }

}
