package com.ridingmate.api_server.domain.activity.facade;

import com.ridingmate.api_server.domain.activity.dto.request.ActivityListRequest;
import com.ridingmate.api_server.domain.activity.dto.response.ActivityListItemResponse;
import com.ridingmate.api_server.domain.activity.dto.response.ActivityListResponse;
import com.ridingmate.api_server.domain.activity.entity.Activity;
import com.ridingmate.api_server.domain.activity.service.ActivityService;
import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.infra.aws.s3.S3Manager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ActivityFacade {

    private final ActivityService activityService;
    private final S3Manager s3Manager;

    /**
     * 사용자별 활동 목록 조회
     * @param authUser 인증된 사용자
     * @param request 활동 목록 조회 요청
     * @return 활동 목록 응답
     */
    public ActivityListResponse getActivityList(AuthUser authUser, ActivityListRequest request) {
        Page<Activity> activityPage = activityService.getActivitiesByUser(authUser.id(), request);

        List<ActivityListItemResponse> activityItems = activityPage.getContent().stream()
                .map(activity -> {

                    String thumbnailImageUrl = activity.getThumbnailImagePath() != null
                            ? s3Manager.getPresignedUrl(activity.getThumbnailImagePath())
                            : null;

                    String userProfileImageUrl = activity.getUser().getProfileImagePath() != null
                            ? s3Manager.getPresignedUrl(activity.getUser().getProfileImagePath())
                            : null;

                    return ActivityListItemResponse.from(
                            activity,
                            thumbnailImageUrl,
                            userProfileImageUrl
                    );
                })
                .toList();

        return ActivityListResponse.of(activityItems, activityPage);
    }
}
