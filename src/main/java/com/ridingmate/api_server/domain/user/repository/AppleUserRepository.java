package com.ridingmate.api_server.domain.user.repository;

import com.ridingmate.api_server.domain.user.entity.AppleUser;
import com.ridingmate.api_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Apple 연동 사용자 Repository
 */
@Repository
public interface AppleUserRepository extends JpaRepository<AppleUser, Long> {

    /**
     * 사용자별 Apple 연동 정보 조회 (활성화된 것만)
     */
    @Query("SELECT au FROM AppleUser au WHERE au.user = :user AND au.isActive = true")
    List<AppleUser> findAllByUserAndIsActiveTrue(@Param("user") User user);

    /**
     * 사용자별 모든 Apple 연동 정보 조회
     */
    List<AppleUser> findAllByUser(User user);

    /**
     * 사용자의 활성화된 Apple 연동 정보 조회 (한 사용자당 하나만)
     */
    @Query("SELECT au FROM AppleUser au WHERE au.user = :user AND au.isActive = true")
    Optional<AppleUser> findByUserAndIsActiveTrue(@Param("user") User user);

    /**
     * 사용자별 활성화된 Apple 연동 개수 조회
     */
    @Query("SELECT COUNT(au) FROM AppleUser au WHERE au.user = :user AND au.isActive = true")
    long countByUserAndIsActiveTrue(@Param("user") User user);

    /**
     * 특정 기간 내에 마지막 동기화된 Apple 연동 정보 조회
     */
    @Query("SELECT au FROM AppleUser au WHERE au.user = :user AND au.isActive = true AND au.lastSyncDate >= :startDate")
    List<AppleUser> findActiveByUserAndLastSyncDateAfter(@Param("user") User user, @Param("startDate") LocalDateTime startDate);

    /**
     * 사용자별 가장 최근 Apple 연동 정보 조회
     */
    @Query("SELECT au FROM AppleUser au WHERE au.user = :user ORDER BY au.createdAt DESC")
    Optional<AppleUser> findMostRecentByUser(@Param("user") User user);

    /**
     * 사용자별 Apple 연동 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(au) > 0 THEN true ELSE false END FROM AppleUser au WHERE au.user = :user AND au.isActive = true")
    boolean existsByUserAndIsActiveTrue(@Param("user") User user);
}
