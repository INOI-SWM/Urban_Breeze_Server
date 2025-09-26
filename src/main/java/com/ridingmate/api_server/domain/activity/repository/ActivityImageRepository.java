package com.ridingmate.api_server.domain.activity.repository;

import com.ridingmate.api_server.domain.activity.entity.Activity;
import com.ridingmate.api_server.domain.activity.entity.ActivityImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityImageRepository extends JpaRepository<ActivityImage, Long> {

    /**
     * 특정 활동의 모든 이미지를 순서대로 조회
     * @param activityId 활동 ID
     * @return 순서대로 정렬된 이미지 목록
     */
    @Query("SELECT ai FROM ActivityImage ai " +
           "WHERE ai.activity.id = :activityId " +
           "ORDER BY ai.displayOrder ASC")
    List<ActivityImage> findByActivityIdOrderByDisplayOrder(@Param("activityId") Long activityId);

    /**
     * 특정 활동의 첫 번째 이미지 (대표 이미지) 조회
     * @param activityId 활동 ID
     * @return 첫 번째 이미지
     */
    @Query("SELECT ai FROM ActivityImage ai " +
           "WHERE ai.activity.id = :activityId " +
           "ORDER BY ai.displayOrder ASC " +
           "LIMIT 1")
    Optional<ActivityImage> findFirstImageByActivityId(@Param("activityId") Long activityId);

    /**
     * 특정 활동의 이미지 개수 조회
     * @param activityId 활동 ID
     * @return 이미지 개수
     */
    @Query("SELECT COUNT(ai) FROM ActivityImage ai WHERE ai.activity.id = :activityId")
    Integer countByActivityId(@Param("activityId") Long activityId);

    /**
     * 특정 활동의 이미지 개수 조회 (Activity 엔티티 사용)
     * @param activity 활동 엔티티
     * @return 이미지 개수
     */
    int countByActivity(Activity activity);

    /**
     * 여러 활동의 첫 번째 이미지들을 한 번에 조회 (배치 처리용)
     * @param activityIds 활동 ID 목록
     * @return 활동별 첫 번째 이미지 맵
     */
    @Query("SELECT ai FROM ActivityImage ai " +
           "WHERE ai.activity.id IN :activityIds " +
           "AND ai.displayOrder = (" +
           "    SELECT MIN(ai2.displayOrder) " +
           "    FROM ActivityImage ai2 " +
           "    WHERE ai2.activity.id = ai.activity.id" +
           ")")
    List<ActivityImage> findFirstImagesByActivityIds(@Param("activityIds") List<Long> activityIds);

    /**
     * 여러 활동의 이미지 개수를 한 번에 조회 (배치 처리용)
     * @param activityIds 활동 ID 목록
     * @return 활동별 이미지 개수
     */
    @Query("SELECT ai.activity.id, COUNT(ai) FROM ActivityImage ai " +
           "WHERE ai.activity.id IN :activityIds " +
           "GROUP BY ai.activity.id")
    List<Object[]> countByActivityIds(@Param("activityIds") List<Long> activityIds);

    /**
     * 특정 활동의 모든 이미지 삭제
     * @param activityId 활동 ID
     */
    void deleteByActivityId(Long activityId);
}
