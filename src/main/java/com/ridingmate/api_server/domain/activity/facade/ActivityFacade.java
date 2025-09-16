package com.ridingmate.api_server.domain.activity.facade;

import com.ridingmate.api_server.domain.activity.dto.request.ActivityListRequest;
import com.ridingmate.api_server.domain.activity.dto.response.ActivityListItemResponse;
import com.ridingmate.api_server.domain.activity.dto.response.ActivityListResponse;
import com.ridingmate.api_server.domain.activity.entity.Activity;
import com.ridingmate.api_server.domain.activity.service.ActivityService;
import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.global.util.GeometryUtil;
import com.ridingmate.api_server.infra.aws.s3.S3Manager;
import com.ridingmate.api_server.infra.geoapify.GeoapifyClient;
import com.ridingmate.api_server.infra.terra.dto.response.TerraPayload;
import com.ridingmate.api_server.infra.terra.TerraMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityFacade {

    private final ActivityService activityService;
    private final S3Manager s3Manager;
    private final GeoapifyClient geoapifyClient;
    private final TerraMapper terraMapper;

    /**
     * Terra 웹훅 데이터로부터 Activity 생성 (썸네일 포함)
     * @param user 사용자
     * @param activityData Terra 활동 데이터
     * @param terraData Terra 원본 데이터 (GPS 좌표 추출용)
     * @return 생성된 Activity (썸네일 경로 포함)
     */
    public Activity createActivityFromTerraData(User user, TerraPayload.Data activityData, TerraPayload.Data terraData) {
        // 1. ActivityService를 통해 순수 도메인 로직으로 Activity 생성
        Activity activity = activityService.createActivityFromTerraData(user, activityData);
        
        // 2. 썸네일 생성 시도 (실패해도 Activity 생성은 계속)
        try {
            Coordinate[] coordinates = terraMapper.toCoordinates(terraData);

            if (coordinates.length >= 2) {
                LineString routeLine = GeometryUtil.createLineStringFromCoordinates(coordinates);

                // Geoapify를 통해 썸네일 생성
                byte[] thumbnailBytes = geoapifyClient.getStaticMap(routeLine);

                // 썸네일 경로 설정 및 S3 업로드
                String thumbnailPath = createThumbnailImagePath(activity.getId());
                activity.updateThumbnailImagePath(thumbnailPath);
                s3Manager.uploadByteFiles(thumbnailPath, thumbnailBytes);
                
                log.info("[Activity] 썸네일 생성 성공: activityId={}, path={}, coordCount={}", 
                        activity.getId(), thumbnailPath, coordinates.length);
            } else {
                log.warn("[Activity] GPS 좌표 부족으로 썸네일 생성 건너뜀: activityId={}, coordCount={}", 
                        activity.getId(), coordinates.length);
            }
        } catch (Exception e) {
            log.error("[Activity] 썸네일 생성 실패 (Activity 생성은 계속): activityId={}, error={}", 
                    activity.getId(), e.getMessage(), e);
        }
        
        return activity;
    }

    /**
     * 썸네일 이미지 경로 생성
     * @param activityId 활동 ID
     * @return 썸네일 이미지 경로
     */
    private String createThumbnailImagePath(Long activityId) {
        String uuid = java.util.UUID.randomUUID().toString();
        return String.format("activity-thumbnails/%d/%s.png", activityId, uuid);
    }

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