package com.ridingmate.api_server.domain.activity.service;

import com.ridingmate.api_server.domain.activity.dto.projection.ActivityDateRangeProjection;
import com.ridingmate.api_server.domain.activity.dto.projection.ActivityStatsProjection;
import com.ridingmate.api_server.domain.activity.dto.projection.GpsLogProjection;
import com.ridingmate.api_server.domain.activity.dto.request.ActivityListRequest;
import com.ridingmate.api_server.domain.activity.dto.request.ActivityStatsRequest;
import com.ridingmate.api_server.domain.activity.dto.request.ManageActivityImagesRequest;
import com.ridingmate.api_server.domain.activity.dto.response.ActivityStatsResponse;
import com.ridingmate.api_server.domain.activity.dto.response.ManageActivityImagesResponse;
import com.ridingmate.api_server.domain.activity.entity.Activity;
import com.ridingmate.api_server.domain.activity.entity.ActivityGpsLog;
import com.ridingmate.api_server.domain.activity.entity.ActivityImage;
import com.ridingmate.api_server.domain.activity.enums.ActivityStatsPeriod;
import com.ridingmate.api_server.domain.activity.exception.ActivityException;
import com.ridingmate.api_server.domain.activity.exception.code.ActivityCommonErrorCode;
import com.ridingmate.api_server.domain.activity.exception.code.ActivityImageErrorCode;
import com.ridingmate.api_server.domain.activity.exception.code.ActivityValidationErrorCode;
import com.ridingmate.api_server.domain.activity.repository.ActivityGpsLogRepository;
import com.ridingmate.api_server.domain.activity.repository.ActivityImageRepository;
import com.ridingmate.api_server.domain.activity.repository.ActivityRepository;
import com.ridingmate.api_server.domain.auth.exception.AuthErrorCode;
import com.ridingmate.api_server.domain.auth.exception.AuthException;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.repository.UserRepository;
import com.ridingmate.api_server.infra.aws.s3.S3Manager;
import com.ridingmate.api_server.infra.terra.dto.response.TerraPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityGpsLogRepository activityGpsLogRepository;
    private final ActivityImageRepository activityImageRepository;
    private final UserRepository userRepository;
    private final S3Manager s3Manager;

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

        // 평균값들 추출
        Integer avgCadence = activityData.cadenceData() != null && activityData.cadenceData().avgCadenceRpm() != null 
                ? activityData.cadenceData().avgCadenceRpm().intValue() : null;
        
        Integer avgHeartRate = activityData.heartRateData() != null && activityData.heartRateData().avgHeartRateBpm() != null 
                ? activityData.heartRateData().avgHeartRateBpm().intValue() : null;
        
        Integer maxHeartRate = activityData.heartRateData() != null && activityData.heartRateData().maxHeartRateBpm() != null 
                ? activityData.heartRateData().maxHeartRateBpm().intValue() : null;
        
        Integer avgPower = activityData.powerData() != null && activityData.powerData().avgPowerWatts() != null 
                ? activityData.powerData().avgPowerWatts().intValue() : null;
        
        Integer maxPower = activityData.powerData() != null && activityData.powerData().maxPowerWatts() != null 
                ? activityData.powerData().maxPowerWatts().intValue() : null;

        Activity activity = Activity.builder()
                .user(user)
                .title(metadata.name() != null ? metadata.name() : "Terra 연동 활동")
                .startedAt(metadata.startTime().toLocalDateTime())
                .endedAt(metadata.endTime().toLocalDateTime())
                .distance(distanceSummary.distanceMeters())
                .duration(Duration.ofSeconds((long) activityData.activeDurationsData().activitySeconds()))
                .elevationGain(distanceSummary.elevation() != null ? distanceSummary.elevation().gainActualMeters() : 0.0)
                .cadence(avgCadence)
                .averageHeartRate(avgHeartRate)
                .maxHeartRate(maxHeartRate)
                .averagePower(avgPower)
                .maxPower(maxPower)
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

        /**
         * 특정 활동의 GPS 좌표 목록을 조회
         * @param activityId 활동 ID
         * @return GPS 좌표 배열 (longitude, latitude, elevation)
         */
        @Transactional(readOnly = true)
        public Coordinate[] getActivityGpsCoordinates(Long activityId) {
            Activity activity = getActivityWithUser(activityId);
            List<Coordinate> coordinateList = activityGpsLogRepository.findCoordinatesByActivity(activity);
            return coordinateList.toArray(new Coordinate[0]);
        }

        /**
         * 특정 활동의 GPS 로그 목록을 조회
         * @param activityId 활동 ID
         * @return GPS 로그 리스트
         */
        @Transactional(readOnly = true)
        public List<ActivityGpsLog> getActivityGpsLogs(Long activityId) {
            Activity activity = getActivityWithUser(activityId);
            return activityGpsLogRepository.findGpsLogsByActivity(activity);
        }

        /**
         * 특정 활동의 GPS 좌표와 상세 정보를 한 번에 조회 (최적화된 방법)
         * @param activityId 활동 ID
         * @return GPS 로그 Projection 리스트
         */
        @Transactional(readOnly = true)
        public List<GpsLogProjection> getActivityGpsLogProjections(Long activityId) {
            Activity activity = getActivityWithUser(activityId);
            return activityGpsLogRepository.findGpsLogProjectionsByActivity(activity);
        }

    /**
     * 사용자의 활동 통계 조회
     * @param userId 사용자 ID
     * @param request 통계 요청
     * @return 활동 통계 응답
     */
    @Transactional(readOnly = true)
    public ActivityStatsResponse getActivityStats(Long userId, ActivityStatsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.AUTHENTICATION_USER_NOT_FOUND));

        // 기간 유효성 검증
        validatePeriod(request);

        // 기간 정보 생성
        ActivityStatsResponse.PeriodInfo periodInfo = createPeriodInfo(request);

        // 전체 기간 통계 조회
        ActivityStatsProjection summaryStats = activityRepository.findActivityStatsByPeriod(
                user.getId(), 
                request.startDate().atStartOfDay(), 
                request.endDate().plusDays(1).atStartOfDay()
        );

        // 요약 정보 생성
        ActivityStatsResponse.SummaryInfo summaryInfo = new ActivityStatsResponse.SummaryInfo(
                summaryStats.getTotalDistanceInKm(),
                summaryStats.totalElevation(),
                summaryStats.getTotalDurationSeconds(),
                summaryStats.count().intValue()
        );

        // 일별 상세 데이터 생성
        List<ActivityStatsResponse.DetailInfo> details = generateDailyDetails(user, request);

        return new ActivityStatsResponse(periodInfo, summaryInfo, details);
    }

    /**
     * 기간 유효성 검증
     */
    private void validatePeriod(ActivityStatsRequest request) {
        LocalDate startDate = request.startDate();
        LocalDate endDate = request.endDate();
        
        switch (request.period()) {
            case MONTH -> {
                // 월간 통계: 시작일은 1일, 종료일은 해당 월의 마지막일이어야 함
                if (startDate.getDayOfMonth() != 1) {
                    throw new ActivityException(ActivityValidationErrorCode.INVALID_MONTH_PERIOD);
                }
                if (!endDate.equals(startDate.withDayOfMonth(startDate.lengthOfMonth()))) {
                    throw new ActivityException(ActivityValidationErrorCode.INVALID_MONTH_PERIOD);
                }
            }
            case YEAR -> {
                // 연간 통계: 시작일은 1월 1일, 종료일은 12월 31일이어야 함
                if (startDate.getMonthValue() != 1 || startDate.getDayOfMonth() != 1) {
                    throw new ActivityException(ActivityValidationErrorCode.INVALID_YEAR_PERIOD);
                }
                if (endDate.getMonthValue() != 12 || endDate.getDayOfMonth() != 31) {
                    throw new ActivityException(ActivityValidationErrorCode.INVALID_YEAR_PERIOD);
                }
            }
            case WEEK -> {
                // 주간 통계는 유연하게 허용 (요청된 기간 그대로 사용)
            }
        }
    }

    /**
     * 기간 정보 생성
     */
    private ActivityStatsResponse.PeriodInfo createPeriodInfo(ActivityStatsRequest request) {
        String type = request.period().name().toLowerCase();
        String displayTitle = generateDisplayTitle(request.period(), request.startDate(), request.endDate());
        
        return new ActivityStatsResponse.PeriodInfo(
                type,
                request.startDate(),
                request.endDate(),
                displayTitle
        );
    }

    /**
     * 표시 제목 생성
     */
    private String generateDisplayTitle(ActivityStatsPeriod period, LocalDate startDate, LocalDate endDate) {
        return switch (period) {
            case WEEK -> String.format("%d년 %d월", startDate.getYear() % 100, startDate.getMonthValue());
            case MONTH -> String.format("%d년 %d월", startDate.getYear() % 100, startDate.getMonthValue());
            case YEAR -> String.format("%d년", startDate.getYear());
        };
    }

    /**
     * 일별 상세 데이터 생성
     */
    private List<ActivityStatsResponse.DetailInfo> generateDailyDetails(User user, ActivityStatsRequest request) {
        return switch (request.period()) {
            case WEEK, MONTH -> generateDailyDetailsForWeekOrMonth(user, request);
            case YEAR -> generateMonthlyDetailsForYear(user, request);
        };
    }

    /**
     * 주간/월간 일별 상세 데이터 생성
     */
    private List<ActivityStatsResponse.DetailInfo> generateDailyDetailsForWeekOrMonth(User user, ActivityStatsRequest request) {
        List<ActivityStatsResponse.DetailInfo> details = new ArrayList<>();
        LocalDate current = request.startDate();
        
        while (!current.isAfter(request.endDate())) {
            // 해당 일의 통계 조회
            ActivityStatsProjection dayStats = activityRepository.findActivityStatsByPeriod(
                    user.getId(),
                    current.atStartOfDay(),
                    current.plusDays(1).atStartOfDay()
            );
            
            // 라벨 생성
            String label = generateDayLabel(request.period(), current);
            
            // 상세 값 생성
            ActivityStatsResponse.DetailValue value = new ActivityStatsResponse.DetailValue(
                    dayStats.getTotalDistanceInKm(),
                    dayStats.totalElevation(),
                    dayStats.getTotalDurationSeconds()
            );
            
            details.add(new ActivityStatsResponse.DetailInfo(label, value));
            current = current.plusDays(1);
        }
        
        return details;
    }

    /**
     * 연간 월별 상세 데이터 생성
     */
    private List<ActivityStatsResponse.DetailInfo> generateMonthlyDetailsForYear(User user, ActivityStatsRequest request) {
        List<ActivityStatsResponse.DetailInfo> details = new ArrayList<>();
        
        // 시작 월부터 종료 월까지 반복
        LocalDate current = request.startDate().withDayOfMonth(1);
        LocalDate endMonth = request.endDate().withDayOfMonth(1);
        
        while (!current.isAfter(endMonth)) {
            // 해당 월의 시작일과 종료일 계산
            LocalDate monthStart = current;
            LocalDate monthEnd = current.withDayOfMonth(current.lengthOfMonth());
            
            // 요청된 기간과 겹치는 부분만 계산
            LocalDate actualStart = monthStart.isBefore(request.startDate()) ? request.startDate() : monthStart;
            LocalDate actualEnd = monthEnd.isAfter(request.endDate()) ? request.endDate() : monthEnd;
            
            // 해당 월의 통계 조회
            ActivityStatsProjection monthStats = activityRepository.findActivityStatsByPeriod(
                    user.getId(),
                    actualStart.atStartOfDay(),
                    actualEnd.plusDays(1).atStartOfDay()
            );
            
            // 라벨 생성 (월)
            String label = generateDayLabel(request.period(), current);
            
            // 상세 값 생성
            ActivityStatsResponse.DetailValue value = new ActivityStatsResponse.DetailValue(
                    monthStats.getTotalDistanceInKm(),
                    monthStats.totalElevation(),
                    monthStats.getTotalDurationSeconds()
            );
            
            details.add(new ActivityStatsResponse.DetailInfo(label, value));
            current = current.plusMonths(1);
        }
        
        return details;
    }

    /**
     * 일별 라벨 생성
     */
    private String generateDayLabel(ActivityStatsPeriod period, LocalDate date) {
        return switch (period) {
            case WEEK, MONTH -> String.valueOf(date.getDayOfMonth());
            case YEAR -> String.valueOf(date.getMonthValue());
        };
    }



    /**
     * 이미지 파일 유효성 검사
     */
    private void validateImageFile(MultipartFile imageFile) {
        if (imageFile.isEmpty()) {
            throw new ActivityException(ActivityImageErrorCode.ACTIVITY_IMAGE_FILE_EMPTY);
        }

        // 파일 크기 확인 (10MB 제한)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (imageFile.getSize() > maxSize) {
            throw new ActivityException(ActivityImageErrorCode.ACTIVITY_IMAGE_FILE_TOO_LARGE);
        }

        // 파일 타입 확인
        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ActivityException(ActivityImageErrorCode.ACTIVITY_IMAGE_INVALID_FORMAT);
        }
    }

    /**
     * 이미지 파일명 생성
     */
    private String generateImageFileName(Long activityId, MultipartFile imageFile) {
        String originalFilename = imageFile.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        
        return String.format("activity_%d_%s%s", activityId, UUID.randomUUID(), extension);
    }

    /**
     * Activity 이미지 전체 관리 (추가/삭제/순서변경)
     * @param userId 사용자 ID
     * @param requestDto 이미지 관리 요청 DTO
     * @param imageFiles 업로드할 이미지 파일들
     * @return 이미지 관리 결과
     */
    @Transactional
    public ManageActivityImagesResponse manageActivityImages(Long userId, ManageActivityImagesRequest requestDto, List<MultipartFile> imageFiles) {
        // Activity 존재 및 소유권 확인
        Activity activity = getActivityWithUser(requestDto.activityId());
        if (!activity.getUser().getId().equals(userId)) {
            throw new ActivityException(ActivityCommonErrorCode.ACTIVITY_ACCESS_DENIED);
        }

        // 유효한 이미지들만 필터링 (삭제되지 않은 이미지들)
        List<ManageActivityImagesRequest.ImageMetaInfo> validImages = requestDto.images().stream()
                .filter(imageInfo -> !imageInfo.isDeleted())
                .collect(Collectors.toList());

        // 최대 이미지 개수 확인 (30장)
        if (validImages.size() > 30) {
            throw new ActivityException(ActivityImageErrorCode.MAX_IMAGE_COUNT_EXCEEDED);
        }

        // 파일명으로 MultipartFile 매핑
        Map<String, MultipartFile> fileMap = imageFiles.stream()
                .collect(Collectors.toMap(MultipartFile::getOriginalFilename, file -> file));

        // 처리 결과 추적
        List<ManageActivityImagesResponse.ImageResult> results = new ArrayList<>();
        int addedCount = 0;
        int deletedCount = 0;
        int reorderedCount = 0;

        // 모든 이미지 처리 (단일 반복문)
        for (ManageActivityImagesRequest.ImageMetaInfo imageInfo : requestDto.images()) {
            if (imageInfo.isImageToDelete()) {
                // 삭제 처리
                deleteActivityImage(imageInfo.imageId());
                deletedCount++;
            } else if (imageInfo.isExistingImage()) {
                // 기존 이미지 순서 업데이트
                ActivityImage existingImage = activityImageRepository.findById(imageInfo.imageId())
                        .orElseThrow(() -> new ActivityException(ActivityCommonErrorCode.ACTIVITY_IMAGE_NOT_FOUND));
                
                // 순서가 변경된 경우
                if (!existingImage.getDisplayOrder().equals(imageInfo.displayOrder())) {
                    existingImage.updateDisplayOrder(imageInfo.displayOrder());
                    activityImageRepository.save(existingImage);
                    reorderedCount++;
                }
                
                results.add(new ManageActivityImagesResponse.ImageResult(
                        existingImage.getId(),
                        existingImage.getImageUrl(),
                        existingImage.getDisplayOrder(),
                        ManageActivityImagesResponse.ProcessStatus.KEPT
                ));
            } else if (imageInfo.imageId() == null && imageInfo.fileName() != null && !imageInfo.isDeleted()) {
                // 새 이미지 추가 (isNewImage() 메서드 제거로 직접 조건 확인)
                MultipartFile imageFile = fileMap.get(imageInfo.fileName());
                if (imageFile == null) {
                    throw new ActivityException(ActivityImageErrorCode.ACTIVITY_IMAGE_FILE_NOT_FOUND);
                }

                // 이미지 파일 유효성 검사
                validateImageFile(imageFile);

                // S3에 이미지 업로드
                String filePath = "activity-image/" + generateImageFileName(requestDto.activityId(), imageFile);
                s3Manager.uploadFile(filePath, imageFile);

                // ActivityImage 엔티티 생성 및 저장
                ActivityImage newImage = ActivityImage.builder()
                        .activity(activity)
                        .imagePath(filePath)
                        .displayOrder(imageInfo.displayOrder())
                        .build();

                ActivityImage savedImage = activityImageRepository.save(newImage);
                addedCount++;

                results.add(new ManageActivityImagesResponse.ImageResult(
                        savedImage.getId(),
                        savedImage.getImageUrl(),
                        savedImage.getDisplayOrder(),
                        ManageActivityImagesResponse.ProcessStatus.ADDED
                ));
            }
        }

        // 결과를 displayOrder 순으로 정렬
        results.sort((a, b) -> Integer.compare(a.displayOrder(), b.displayOrder()));

        // 처리 결과 요약
        ManageActivityImagesResponse.ProcessSummary summary = new ManageActivityImagesResponse.ProcessSummary(
                addedCount,
                deletedCount,
                reorderedCount,
                results.size()
        );

        return ManageActivityImagesResponse.of(requestDto.activityId(), results, summary);
    }

    /**
     * Activity 이미지 삭제
     */
    private void deleteActivityImage(Long imageId) {
        ActivityImage image = activityImageRepository.findById(imageId)
                .orElseThrow(() -> new ActivityException(ActivityCommonErrorCode.ACTIVITY_IMAGE_NOT_FOUND));
        
        // S3에서 이미지 삭제 (선택사항 - 비용 절약을 위해 보관할 수도 있음)
        // s3Manager.deleteFile(image.getImagePath());
        
        // DB에서 이미지 삭제
        activityImageRepository.delete(image);
    }

    /**
     * 썸네일을 activity_images 테이블에 추가
     * @param activity 활동 엔티티
     * @param thumbnailPath 썸네일 이미지 경로
     */
    @Transactional
    public void addThumbnailToActivityImages(Activity activity, String thumbnailPath) {
        ActivityImage thumbnailImage = ActivityImage.builder()
                .activity(activity)
                .imagePath(thumbnailPath)
                .displayOrder(0)
                .build();
        
        activityImageRepository.save(thumbnailImage);
        
        log.info("[Activity] 썸네일을 activity_images에 추가: activityId={}, imageId={}, path={}",
                activity.getId(), thumbnailImage.getId(), thumbnailPath);
    }

    /**
     * 활동 제목 변경
     * @param userId 사용자 ID
     * @param activityId 활동 ID
     * @param newTitle 새로운 제목
     * @return 변경된 활동 정보
     */
    @Transactional
    public Activity updateActivityTitle(Long userId, Long activityId, String newTitle) {
        // Activity 존재 및 소유권 확인
        Activity activity = getActivityWithUser(activityId);
        if (!activity.getUser().getId().equals(userId)) {
            throw new ActivityException(ActivityCommonErrorCode.ACTIVITY_ACCESS_DENIED);
        }

        // 제목 변경
        activity.updateTitle(newTitle);
        Activity savedActivity = activityRepository.save(activity);

        log.info("[Activity] 활동 제목 변경: activityId={}, userId={}, oldTitle={}, newTitle={}",
                activityId, userId, activity.getTitle(), newTitle);

        return savedActivity;
    }

}
