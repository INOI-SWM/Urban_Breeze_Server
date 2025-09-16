package com.ridingmate.api_server.domain.activity.repository;

import com.ridingmate.api_server.domain.activity.entity.Activity;
import com.ridingmate.api_server.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
           "WHERE a.user = :user")
    Page<Activity> findByUserWithSort(@Param("user") User user, Pageable pageable);

    /**
     * 특정 활동과 연관된 사용자 정보를 함께 조회
     * @param activityId 활동 ID
     * @return Activity with User
     */
    @Query("SELECT a FROM Activity a " +
           "JOIN FETCH a.user " +
           "WHERE a.id = :activityId")
    Activity findActivityWithUser(@Param("activityId") Long activityId);
}