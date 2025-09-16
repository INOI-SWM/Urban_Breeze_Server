package com.ridingmate.api_server.domain.activity.service;

import com.ridingmate.api_server.domain.activity.dto.request.ActivityListRequest;
import com.ridingmate.api_server.domain.activity.entity.Activity;
import com.ridingmate.api_server.domain.activity.entity.ActivityImage;
import com.ridingmate.api_server.domain.activity.exception.ActivityException;
import com.ridingmate.api_server.domain.activity.exception.code.ActivityCommonErrorCode;
import com.ridingmate.api_server.domain.activity.repository.ActivityImageRepository;
import com.ridingmate.api_server.domain.activity.repository.ActivityRepository;
import com.ridingmate.api_server.domain.auth.exception.AuthErrorCode;
import com.ridingmate.api_server.domain.auth.exception.AuthException;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.repository.UserRepository;
import com.ridingmate.api_server.infra.terra.dto.response.TerraPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityImageRepository activityImageRepository;
    private final UserRepository userRepository;

    /**
     * Terra 웹훅 데이터로부터 Activity 생성 (순수 도메인 로직)
     * @param user 사용자
     * @param activityData Terra 활동 데이터
     * @return 생성된 Activity
     */
    @Transactional
    public Activity createActivityFromTerraData(User user, TerraPayload.Data activityData) {
        TerraPayload.Metadata metadata = activityData.metadata();
        TerraPayload.DistanceData.Summary distanceSummary = activityData.distanceData().summary();

        Activity activity = Activity.builder()
                .user(user)
                .title(metadata.name() != null ? metadata.name() : "Terra 연동 활동")
                .startedAt(metadata.startTime().toLocalDateTime())
                .endedAt(metadata.endTime().toLocalDateTime())
                .distance(distanceSummary.distanceMeters())
                .duration(Duration.ofSeconds((long) activityData.activeDurationsData().activitySeconds()))
                .elevationGain(distanceSummary.elevation() != null ? distanceSummary.elevation().gainActualMeters() : 0.0)
                .build();

        // Activity 저장
        return activityRepository.save(activity);
    }

    /**
     * 사용자별 활동 목록을 페이징하여 조회
     * @param userId 사용자 ID
     * @param request 활동 목록 조회 요청
     * @return 활동 페이지
     */
    @Transactional(readOnly = true)
    public Page<Activity> getActivitiesByUser(Long userId, ActivityListRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.AUTHENTICATION_USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(request.page(), request.size(), request.sortType().getSort());
        return activityRepository.findByUserWithSort(user, pageable);
    }

    // 이미지 관련 배치 조회 메서드들 제거 (Activity에 thumbnailImagePath 추가로 불필요)

    /**
     * 특정 활동의 상세 정보를 사용자 정보와 함께 조회
     * @param activityId 활동 ID
     * @return Activity with User
     */
    @Transactional(readOnly = true)
    public Activity getActivityWithUser(Long activityId) {
        Activity activity = activityRepository.findActivityWithUser(activityId);
        if (activity == null) {
            throw new ActivityException(ActivityCommonErrorCode.ACTIVITY_NOT_FOUND);
        }
        return activity;
    }

    /**
     * 특정 활동의 모든 이미지를 순서대로 조회
     * @param activityId 활동 ID
     * @return 순서대로 정렬된 이미지 목록
     */
    @Transactional(readOnly = true)
    public List<ActivityImage> getActivityImages(Long activityId) {
        return activityImageRepository.findByActivityIdOrderByDisplayOrder(activityId);
    }
}
