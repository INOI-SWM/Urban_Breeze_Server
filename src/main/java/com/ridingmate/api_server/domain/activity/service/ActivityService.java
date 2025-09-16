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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityImageRepository activityImageRepository;
    private final UserRepository userRepository;

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
