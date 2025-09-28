package com.ridingmate.api_server.domain.activity.facade;

import com.ggalmazor.ltdownsampling.Point;
import com.ridingmate.api_server.domain.activity.dto.request.ActivityListRequest;
import com.ridingmate.api_server.domain.activity.dto.request.ActivityStatsRequest;
import com.ridingmate.api_server.domain.activity.dto.request.AppleWorkoutsImportRequest;
import com.ridingmate.api_server.domain.activity.dto.request.UpdateActivityTitleRequest;
import com.ridingmate.api_server.domain.activity.dto.response.*;
import com.ridingmate.api_server.domain.activity.entity.Activity;
import com.ridingmate.api_server.domain.activity.entity.ActivityGpsLog;
import com.ridingmate.api_server.domain.activity.dto.projection.GpsLogProjection;
import com.ridingmate.api_server.domain.activity.entity.ActivityImage;
import com.ridingmate.api_server.domain.activity.exception.ActivityException;
import com.ridingmate.api_server.domain.activity.exception.code.ActivityCommonErrorCode;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

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
                s3Manager.uploadByteFiles(thumbnailPath, thumbnailBytes, "image/png");
                
                // 썸네일을 activity_images 테이블에도 추가 (displayOrder = 0으로 설정하여 가장 앞에 표시)
                activityService.addThumbnailToActivityImages(activity, thumbnailPath);
                
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

    /**
     * 활동 상세 정보 조회
     * @param activityId 활동 ID
     * @return 활동 상세 응답
     */
    public ActivityDetailResponse getActivityDetail(String activityId) {
        Activity activity = activityService.getActivityWithUserByActivityId(activityId);

        List<GpsLogProjection> gpsLogProjections = activityService.getActivityGpsLogProjections(activity);

        Coordinate[] coordinates = gpsLogProjections.stream()
                .map(GpsLogProjection::toCoordinate)
                .toArray(Coordinate[]::new);
        
        List<Point> elevationPoints =
                GeometryUtil.downsampleElevationProfile(coordinates, activity.getDistance() / 1000.0);

        List<ActivityImage> activityImages = activityService.getActivityImages(activity.getId());
        List<ActivityDetailResponse.ActivityImageResponse> imageResponses = activityImages.stream()
                .map(image -> ActivityDetailResponse.ActivityImageResponse.from(
                        image,
                        s3Manager.getPresignedUrl(image.getImagePath())
                ))
                .collect(Collectors.toList());

        // S3 URL 생성
        String profileImageUrl = activity.getUser().getProfileImagePath() != null
                ? s3Manager.getPresignedUrl(activity.getUser().getProfileImagePath())
                : null;

        String thumbnailImageUrl = activity.getThumbnailImagePath() != null
                ? s3Manager.getPresignedUrl(activity.getThumbnailImagePath())
                : null;

        // Bounding Box 계산
        List<Double> bbox = GeometryUtil.calculateBoundingBoxList(coordinates);

        return ActivityDetailResponse.from(
                activity,
                elevationPoints,
                coordinates,
                gpsLogProjections,
                profileImageUrl,
                thumbnailImageUrl,
                imageResponses,
                bbox
        );
    }

    /**
     * 사용자의 활동 통계 조회
     * @param authUser 인증된 사용자
     * @param request 통계 요청
     * @return 활동 통계 응답
     */
    public ActivityStatsResponse getActivityStats(AuthUser authUser, ActivityStatsRequest request) {
        return activityService.getActivityStats(authUser.id(), request);
    }



    /**
     * 활동 이미지 업로드
     * @param authUser 인증된 사용자
     * @param activityId 활동 ID
     * @param files 업로드할 이미지 파일들
     * @return 업로드 결과
     */
    public UploadActivityImagesResponse uploadActivityImages(AuthUser authUser, String activityId, List<MultipartFile> files) {
        Activity activity = activityService.getActivityWithUserByActivityId(activityId);
        List<ActivityImage> uploadImages = activityService.uploadActivityImages(authUser.id(), activity.getId(), files);

        List<ActivityImageResponse> responses = uploadImages.stream()
                .map(activityImage -> ActivityImageResponse.of(
                        activityImage,
                        s3Manager.getPresignedUrl(activityImage.getImagePath())
                ))
                .toList();

        return UploadActivityImagesResponse.from(responses);
    }

    /**
     * 활동 이미지 삭제
     * @param authUser 인증된 사용자
     * @param activityId 활동 ID
     * @param imageId 삭제할 이미지 ID
     * @return 삭제 결과
     */
    public DeleteActivityImageResponse deleteActivityImage(AuthUser authUser, String activityId, Long imageId) {
        Activity activity = activityService.getActivityWithUserByActivityId(activityId);
        return activityService.deleteActivityImage(authUser.id(), activity.getId(), imageId);
    }

    /**
     * 활동 제목 변경
     * @param authUser 인증된 사용자
     * @param activityId 활동 ID
     * @param request 제목 변경 요청
     * @return 제목 변경 결과
     */
    public UpdateActivityTitleResponse updateActivityTitle(AuthUser authUser, String activityId, UpdateActivityTitleRequest request) {
        Activity activity = activityService.getActivityWithUserByActivityId(activityId);
        Activity updatedActivity = activityService.updateActivityTitle(authUser.id(), activity.getId(), request.title());
        return UpdateActivityTitleResponse.of(updatedActivity.getActivityId().toString(), updatedActivity.getTitle());
    }

    /**
     * 주행 기록 삭제
     * @param authUser 인증된 사용자
     * @param activityId 삭제할 주행 기록 ID
     */
    public void deleteActivity(AuthUser authUser, String activityId) {
        log.info("주행 기록 삭제 시작: userId={}, activityId={}", authUser.id(), activityId);
        
        // 1. 주행 기록 조회 및 권한 확인
        Activity activity = activityService.getActivityWithUserByActivityId(activityId);
        
        // 2. 소유자 확인
        if (!activity.getUser().getId().equals(authUser.id())) {
            throw new ActivityException(ActivityCommonErrorCode.ACTIVITY_ACCESS_DENIED);
        }
        
        // 3. 관련 데이터 삭제
        activityService.deleteActivity(activity);
        
        log.info("주행 기록 삭제 완료: userId={}, activityId={}", authUser.id(), activityId);
    }

    /**
     * Apple HealthKit 운동 기록 업로드
     * @param authUser 인증된 사용자
     * @param request Apple 운동 기록 업로드 요청
     * @return 업로드된 운동 기록 목록
     */
    public AppleWorkoutsImportResponse importAppleWorkouts(AuthUser authUser, AppleWorkoutsImportRequest request) {
        log.info("Apple 운동 기록 업로드 시작: userId={}, count={}", authUser.id(), request.workouts().size());
        
        AppleWorkoutsImportResponse response = activityService.importAppleWorkouts(authUser.id(), request);
        
        // 업로드된 각 운동 기록에 대해 썸네일 생성 시도
        for (var activityResponse : response.activities()) {
            try {
                generateThumbnailForAppleActivity(activityResponse.activityId());
            } catch (Exception e) {
                log.error("Apple 운동 기록 썸네일 생성 실패: activityId={}, error={}", 
                        activityResponse.activityId(), e.getMessage(), e);
            }
        }
        
        log.info("Apple 운동 기록 업로드 완료: userId={}, successCount={}", 
                authUser.id(), response.successCount());
        
        return response;
    }

    /**
     * Apple 운동 기록을 위한 썸네일 생성
     */
    private void generateThumbnailForAppleActivity(String activityId) {
        try {
            // Activity와 GPS 로그 조회
            Activity activity = activityService.getActivityWithUserByActivityId(activityId);
            List<ActivityGpsLog> gpsLogs = activityService.getActivityGpsLogs(activity.getId());
            
            if (gpsLogs == null || gpsLogs.isEmpty()) {
                log.warn("Apple 운동 기록 썸네일 생성 건너뜀: activityId={}, gpsLogs가 비어있음", activityId);
                return;
            }

            // GPS 좌표를 Coordinate 배열로 변환
            Coordinate[] coordinates = gpsLogs.stream()
                    .map(gpsLog -> new Coordinate(gpsLog.getLongitude(), gpsLog.getLatitude()))
                    .toArray(Coordinate[]::new);

            if (coordinates.length < 2) {
                log.warn("Apple 운동 기록 썸네일 생성 건너뜀: activityId={}, 좌표 부족 (count={})", 
                        activityId, coordinates.length);
                return;
            }

            // LineString 생성
            LineString routeLine = GeometryUtil.createLineStringFromCoordinates(coordinates);

            // Geoapify를 통해 썸네일 생성
            byte[] thumbnailBytes = geoapifyClient.getStaticMap(routeLine);

            // 썸네일 경로 생성 및 S3 업로드
            String thumbnailPath = createThumbnailImagePath(activity.getId());
            activity.updateThumbnailImagePath(thumbnailPath);
            s3Manager.uploadByteFiles(thumbnailPath, thumbnailBytes, "image/png");
            
            // 썸네일을 activity_images 테이블에도 추가
            activityService.addThumbnailToActivityImages(activity, thumbnailPath);
            
            log.info("Apple 운동 기록 썸네일 생성 성공: activityId={}, path={}, coordCount={}", 
                    activityId, thumbnailPath, coordinates.length);

        } catch (Exception e) {
            log.error("Apple 운동 기록 썸네일 생성 실패: activityId={}, error={}", 
                    activityId, e.getMessage(), e);
            throw e; // 상위로 예외 전파
        }
    }
}