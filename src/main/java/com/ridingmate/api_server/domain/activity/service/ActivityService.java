package com.ridingmate.api_server.domain.activity.service;

import com.ridingmate.api_server.domain.activity.dto.projection.ActivityStatsProjection;
import com.ridingmate.api_server.domain.activity.dto.projection.GpsLogProjection;
import com.ridingmate.api_server.domain.activity.dto.projection.YearlyStatsProjection;
import com.ridingmate.api_server.domain.activity.dto.request.ActivityListRequest;
import com.ridingmate.api_server.domain.activity.dto.request.ActivityStatsRequest;
import com.ridingmate.api_server.domain.activity.dto.request.AppleWorkoutImportRequest;
import com.ridingmate.api_server.domain.activity.dto.request.AppleWorkoutsImportRequest;
import com.ridingmate.api_server.domain.activity.dto.response.ActivityStatsResponse;
import com.ridingmate.api_server.domain.activity.dto.response.AppleWorkoutImportResponse;
import com.ridingmate.api_server.domain.activity.dto.response.AppleWorkoutsImportResponse;
import com.ridingmate.api_server.domain.activity.dto.response.DeleteActivityImageResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

        // 칼로리 정보 추출
        Double calories = activityData.caloriesData() != null && activityData.caloriesData().totalBurnedCalories() != null 
                ? activityData.caloriesData().totalBurnedCalories() : null;

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
                .calories(calories)
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
     * activityId로 활동 조회 (User 정보 함께)
     * @param activityId UUID 기반 활동 ID
     * @return Activity with User
     */
    @Transactional(readOnly = true)
    public Activity getActivityWithUserByActivityId(String activityId) {
        return activityRepository.findByActivityId(UUID.fromString(activityId))
                .orElseThrow(() -> new ActivityException(ActivityCommonErrorCode.ACTIVITY_NOT_FOUND));
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
         * @param activity 활동
         * @return GPS 로그 Projection 리스트
         */
        @Transactional(readOnly = true)
        public List<GpsLogProjection> getActivityGpsLogProjections(Activity activity) {
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
        ActivityStatsResponse.PeriodInfo periodInfo = createPeriodInfo(request, user);

        // 전체 기간 통계 조회
        ActivityStatsProjection summaryStats = getSummaryStats(user, request);

        // 요약 정보 생성
        ActivityStatsResponse.SummaryInfo summaryInfo = new ActivityStatsResponse.SummaryInfo(
                summaryStats.getTotalDistanceInKm(),
                summaryStats.totalElevation(),
                summaryStats.getTotalDurationSeconds(),
                summaryStats.count().intValue()
        );

        // 일별 상세 데이터 생성
        List<ActivityStatsResponse.DetailInfo> details = generateDailyDetails(user, request);

        // 가장 오래된 활동 날짜 조회
        LocalDateTime oldestActivityDateTime = activityRepository.findOldestActivityDate(user);
        LocalDate oldestActivityDate = oldestActivityDateTime != null ? oldestActivityDateTime.toLocalDate() : null;

        return ActivityStatsResponse.of(periodInfo, summaryInfo, details, oldestActivityDate);
    }

    /**
     * 기간별 요약 통계 조회
     */
    private ActivityStatsProjection getSummaryStats(User user, ActivityStatsRequest request) {
        return switch (request.period()) {
            case WEEK, MONTH, YEAR -> activityRepository.findActivityStatsByPeriod(
                    user.getId(), 
                    request.startDate().atStartOfDay(), 
                    request.endDate().plusDays(1).atStartOfDay()
            );
            case ALL -> {
                // ALL 기간일 때: 가장 오래된 기록부터 오늘까지
                LocalDateTime oldestActivityDateTime = activityRepository.findOldestActivityDate(user);
                LocalDateTime startDateTime = oldestActivityDateTime != null ? 
                        oldestActivityDateTime : LocalDateTime.now().minusYears(1);
                LocalDateTime endDateTime = LocalDateTime.now();
                
                yield activityRepository.findActivityStatsByPeriod(
                        user.getId(), 
                        startDateTime, 
                        endDateTime
                );
            }
        };
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
            case ALL -> {
                // 전체 통계는 유연하게 허용 (요청된 기간 그대로 사용)
            }
        }
    }

    /**
     * 기간 정보 생성
     */
    private ActivityStatsResponse.PeriodInfo createPeriodInfo(ActivityStatsRequest request, User user) {
        String type = request.period().name().toLowerCase();
        
        // ALL 기간일 때는 실제 날짜 범위 계산
        LocalDate startDate, endDate;
        if (request.period() == ActivityStatsPeriod.ALL) {
            // ALL 기간일 때: 가장 오래된 기록부터 오늘까지
            LocalDateTime oldestActivityDateTime = activityRepository.findOldestActivityDate(user);
            startDate = oldestActivityDateTime != null ? 
                    oldestActivityDateTime.toLocalDate() : LocalDate.now().minusYears(1);
            endDate = LocalDate.now();
        } else {
            startDate = request.startDate();
            endDate = request.endDate();
        }
        
        String displayTitle = generateDisplayTitle(request.period(), startDate, endDate);
        
        return new ActivityStatsResponse.PeriodInfo(
                type,
                startDate,
                endDate,
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
            case ALL -> "전체";
        };
    }

    /**
     * 일별 상세 데이터 생성
     */
    private List<ActivityStatsResponse.DetailInfo> generateDailyDetails(User user, ActivityStatsRequest request) {
        return switch (request.period()) {
            case WEEK, MONTH -> generateDailyDetailsForWeekOrMonth(user, request);
            case YEAR -> generateMonthlyDetailsForYear(user, request);
            case ALL -> generateYearlyStats(user);
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
            case ALL -> String.valueOf(date.getYear());
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
    private static String generateImageFileName(MultipartFile imageFile) {
        String originalFilename = imageFile.getOriginalFilename();
        
        return String.format("%s%s", UUID.randomUUID(), originalFilename);
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

    /**
     * 활동 이미지 업로드
     * @param userId 사용자 ID
     * @param activityId 활동 ID
     * @param files 업로드할 이미지 파일들
     * @return 업로드 결과
     */
    @Transactional
    public List<ActivityImage> uploadActivityImages(Long userId, Long activityId, List<MultipartFile> files) {
        // Activity 존재 및 소유권 확인
        Activity activity = getActivityWithUser(activityId);
        if (!activity.getUser().getId().equals(userId)) {
            throw new ActivityException(ActivityCommonErrorCode.ACTIVITY_ACCESS_DENIED);
        }

        // 현재 이미지 개수 확인
        int currentImageCount = activityImageRepository.countByActivity(activity);
        if (currentImageCount + files.size() > 30) {
            throw new ActivityException(ActivityImageErrorCode.MAX_IMAGE_COUNT_EXCEEDED);
        }

        List<ActivityImage> uploadedImages = new ArrayList<>();
        
        for (MultipartFile imageFile : files) {
            // 이미지 파일 유효성 검증
            validateImageFile(imageFile);

            String imagePath = generateImageFileName(imageFile);
            s3Manager.uploadFile(imagePath, imageFile);
            
            // 표시 순서 자동 할당 (업로드 순서대로)
            Integer displayOrder = currentImageCount + uploadedImages.size() + 1;

            ActivityImage activityImage = ActivityImage.builder()
                    .imagePath(imagePath)
                    .activity(activity)
                    .displayOrder(displayOrder)
                    .build();
            
            ActivityImage savedImage = activityImageRepository.save(activityImage);
            uploadedImages.add(savedImage);
        }

        return uploadedImages;
    }

    /**
     * 활동 이미지 삭제
     * @param userId 사용자 ID
     * @param activityId 활동 ID
     * @param imageId 삭제할 이미지 ID
     * @return 삭제 결과
     */
    @Transactional
    public DeleteActivityImageResponse deleteActivityImage(Long userId, Long activityId, Long imageId) {
        // Activity 존재 및 소유권 확인
        Activity activity = getActivityWithUser(activityId);
        if (!activity.getUser().getId().equals(userId)) {
            throw new ActivityException(ActivityCommonErrorCode.ACTIVITY_ACCESS_DENIED);
        }

        // 이미지 존재 및 소유권 확인
        ActivityImage activityImage = activityImageRepository.findById(imageId)
                .orElseThrow(() -> new ActivityException(ActivityCommonErrorCode.ACTIVITY_IMAGE_NOT_FOUND));
        
        if (!activityImage.getActivity().getId().equals(activityId)) {
            throw new ActivityException(ActivityCommonErrorCode.ACTIVITY_ACCESS_DENIED);
        }
        
        // S3에서 이미지 삭제
        s3Manager.deleteFile(activityImage.getImagePath());
        
        // DB에서 이미지 삭제
        activityImageRepository.delete(activityImage);
        
        return DeleteActivityImageResponse.from(imageId);
    }

    /**
     * 사용자 삭제 시 활동 데이터 처리
     * - 활동 데이터는 법정 기간 동안 보존 (삭제하지 않음)
     * - 사용자 정보만 마스킹 처리
     */
    @Transactional
    public void handleUserDeletion(User user) {
        log.info("활동 데이터 처리 시작: userId={}", user.getId());
        
        try {
            // 1. 사용자의 모든 활동 조회 (삭제된 활동 포함)
            List<Activity> userActivities = activityRepository.findByUser(user);
            
            // 2. 활동 정보 마스킹 처리
            maskActivityUserInfo(userActivities);
            
            // 3. 활동 이미지 처리 (S3에서 삭제)
            handleActivityImages(userActivities);
            
            // 4. 활동 GPS 로그 처리 (개인정보 즉시 파기)
            handleActivityGpsLogs(userActivities);
            
            log.info("활동 데이터 처리 완료: userId={}, count={}", user.getId(), userActivities.size());
        } catch (Exception e) {
            log.error("활동 데이터 처리 중 오류 발생: userId={}", user.getId(), e);
            // 활동 데이터 처리 실패해도 사용자 삭제는 계속 진행
        }
    }

    /**
     * 활동 정보 마스킹 및 소프트 삭제 처리
     * - 모든 개인정보 관련 필드 마스킹/제거
     * - 통계용 데이터만 보존
     */
    private void maskActivityUserInfo(List<Activity> activities) {
        log.info("활동 정보 마스킹 및 소프트 삭제 처리 시작: count={}", activities.size());
        
        for (Activity activity : activities) {
            // 모든 개인정보 필드 마스킹 및 소프트 삭제 처리 (통합)
            activity.maskPersonalDataForDeletion();
            
            log.debug("활동 정보 마스킹 및 소프트 삭제: activityId={}", activity.getId());
        }
        
        log.info("활동 정보 마스킹 및 소프트 삭제 처리 완료: count={}", activities.size());
    }

    /**
     * 활동 이미지 처리 (S3에서 삭제)
     * - ActivityImage 테이블의 모든 이미지를 삭제 (썸네일 포함)
     */
    private void handleActivityImages(List<Activity> activities) {
        log.info("활동 이미지 처리 시작: count={}", activities.size());
        
        for (Activity activity : activities) {
            try {
                // ActivityImage 테이블의 모든 이미지 조회 및 삭제
                deleteAllActivityImages(activity);
                
            } catch (Exception e) {
                log.warn("활동 이미지 처리 중 오류: activityId={}", activity.getId(), e);
            }
        }
        
        log.info("활동 이미지 처리 완료: count={}", activities.size());
    }

    /**
     * 활동의 모든 이미지 삭제 (썸네일 포함)
     */
    private void deleteAllActivityImages(Activity activity) {
        try {
            // 활동의 모든 이미지 조회 (썸네일 포함)
            List<ActivityImage> activityImages = activityImageRepository.findByActivityIdOrderByDisplayOrder(activity.getId());
            
            log.debug("활동 이미지 삭제 시작: activityId={}, count={}", activity.getId(), activityImages.size());
            
            for (ActivityImage activityImage : activityImages) {
                try {
                    // S3에서 이미지 삭제 (썸네일 포함 모든 이미지)
                    s3Manager.deleteFile(activityImage.getImagePath());
                    log.debug("활동 이미지 삭제: activityId={}, imageId={}, path={}", 
                        activity.getId(), activityImage.getId(), activityImage.getImagePath());
                } catch (Exception e) {
                    log.warn("활동 이미지 삭제 실패: activityId={}, imageId={}, path={}", 
                        activity.getId(), activityImage.getId(), activityImage.getImagePath(), e);
                }
            }
            
            // DB에서 모든 이미지 삭제 (썸네일 포함)
            activityImageRepository.deleteByActivityId(activity.getId());
            
            log.debug("활동 이미지 DB 삭제 완료: activityId={}", activity.getId());
            
        } catch (Exception e) {
            log.warn("활동 이미지 삭제 중 오류: activityId={}", activity.getId(), e);
        }
    }

    /**
     * 활동 GPS 로그 처리 (개인정보 즉시 파기)
     * - 좌표 데이터: 즉시 파기 (원본 위치)
     * - 시간 정보: 즉시 파기 (동선 복원 가능)
     * - 생체 정보: 즉시 파기 (건강/민감 성격)
     * - 성능 데이터: 즉시 파기 (개인 성능 특성)
     */
    private void handleActivityGpsLogs(List<Activity> activities) {
        log.info("활동 GPS 로그 처리 시작: count={}", activities.size());
        
        for (Activity activity : activities) {
            try {
                // 활동의 모든 GPS 로그 조회
                deleteAllActivityGpsLogs(activity);
            } catch (Exception e) {
                log.warn("활동 GPS 로그 처리 중 오류: activityId={}", activity.getId(), e);
            }
        }
        
        log.info("활동 GPS 로그 처리 완료: count={}", activities.size());
    }

    /**
     * 주행 기록 삭제 (개별 삭제)
     * @param activity 삭제할 주행 기록
     */
    @Transactional
    public void deleteActivity(Activity activity) {
        log.info("주행 기록 삭제 시작: activityId={}", activity.getId());
        
        try {
            deleteAllActivityImages(activity);
            deleteAllActivityGpsLogs(activity);

            activity.maskPersonalDataForDeletion();

            log.info("주행 기록 삭제 완료: activityId={}", activity.getId());
            
        } catch (Exception e) {
            log.error("주행 기록 삭제 중 오류 발생: activityId={}", activity.getId(), e);
            throw new ActivityException(ActivityCommonErrorCode.ACTIVITY_DELETE_FAILED);
        }
    }

    private void deleteAllActivityGpsLogs(Activity activity){
        List<ActivityGpsLog> activityGpsLogs = activityGpsLogRepository.findByActivityIdOrderByLogTimeAsc(activity.getId());

        log.debug("주행 기록 GPS 로그 삭제 시작: activityId={}, count={}", activity.getId(), activityGpsLogs.size());

        // DB에서 모든 GPS 로그 하드 삭제
        activityGpsLogRepository.deleteByActivityId(activity.getId());

        log.debug("주행 기록 GPS 로그 하드 삭제 완료: activityId={}", activity.getId());
    }

    /**
     * 연별 통계 데이터 생성 (ALL 기간용)
     */
    private List<ActivityStatsResponse.DetailInfo> generateYearlyStats(User user) {
        List<YearlyStatsProjection> yearlyProjections = activityRepository.findYearlyActivityStats(user.getId());
        
        return yearlyProjections.stream()
                .map(yearlyProjection -> {
                    // 연도 라벨 생성 (예: "2025")
                    String yearLabel = yearlyProjection.getYear().toString();
                    
                    // 연도별 통계 값 생성
                    ActivityStatsResponse.DetailValue yearValue = new ActivityStatsResponse.DetailValue(
                            yearlyProjection.getTotalDistance() / 1000.0, // 미터를 킬로미터로 변환
                            yearlyProjection.getTotalElevation(),
                            yearlyProjection.getTotalDurationSeconds()
                    );
                    
                    return new ActivityStatsResponse.DetailInfo(yearLabel, yearValue);
                })
                .toList();
    }

    /**
     * Apple HealthKit 운동 기록 업로드
     * @param user 사용자
     * @param request Apple 운동 기록 업로드 요청
     * @return 업로드된 운동 기록 목록
     */
    @Transactional
    public AppleWorkoutsImportResponse importAppleWorkouts(User user, AppleWorkoutsImportRequest request) {
        log.info("Apple 운동 기록 업로드 시작: userId={}, count={}", user.getId(), request.workouts().size());

        List<AppleWorkoutImportResponse> importedActivities = new ArrayList<>();

        for (AppleWorkoutImportRequest workoutRequest : request.workouts()) {
            try {
                // 1. Activity 먼저 생성 및 저장
                Activity activity = createActivityFromAppleWorkout(user, workoutRequest);
                Activity savedActivity = activityRepository.save(activity);
                
                // 2. GPS 로그 생성 및 저장
                List<ActivityGpsLog> gpsLogs = createActivityGpsLogsFromAppleWorkout(savedActivity, workoutRequest);
                
                if (!gpsLogs.isEmpty()) {
                    activityGpsLogRepository.saveAll(gpsLogs);
                }
                

                AppleWorkoutImportResponse response = AppleWorkoutImportResponse.from(savedActivity, gpsLogs.size());
                importedActivities.add(response);

                log.info("Apple 운동 기록 업로드 성공: activityId={}, title={}", 
                        savedActivity.getActivityId(), savedActivity.getTitle());

            } catch (Exception e) {
                log.error("Apple 운동 기록 업로드 실패: userId={}, title={}", 
                        user.getId(), workoutRequest.title(), e);
                // 개별 실패는 로그만 남기고 계속 진행
            }
        }

        log.info("Apple 운동 기록 업로드 완료: userId={}, successCount={}", 
                user.getId(), importedActivities.size());

        return AppleWorkoutsImportResponse.of(importedActivities);
    }

    /**
     * Apple HealthKit 운동 데이터로부터 Activity 생성
     */
    private Activity createActivityFromAppleWorkout(User user, AppleWorkoutImportRequest request) {
        // 심박수 데이터에서 평균/최대값 계산
        Integer averageHeartRate = null;
        Integer maxHeartRate = null;
        if (request.heartRateData() != null && !request.heartRateData().isEmpty()) {
            List<Integer> heartRates = request.heartRateData().stream()
                    .map(AppleWorkoutImportRequest.HeartRateSample::heartRate)
                    .toList();
            
            averageHeartRate = (int) heartRates.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);
            
            maxHeartRate = heartRates.stream()
                    .mapToInt(Integer::intValue)
                    .max()
                    .orElse(0);
        }

        // 위치 데이터에서 고도 변화 계산 (elevationGain)
        Double elevationGain = null;
        if (request.locationData() != null && !request.locationData().isEmpty()) {
            List<Double> altitudes = request.locationData().stream()
                    .filter(location -> location.altitude() != null)
                    .map(AppleWorkoutImportRequest.LocationData::altitude)
                    .toList();
            
            if (!altitudes.isEmpty()) {
                double minAltitude = altitudes.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                double maxAltitude = altitudes.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                elevationGain = Math.max(0.0, maxAltitude - minAltitude);
            }
        }

        return Activity.builder()
                .user(user)
                .title(request.title())
                .startedAt(request.startTime())
                .endedAt(request.endTime())
                .distance(request.distance())
                .duration(request.getDuration())
                .elevationGain(elevationGain)
                .cadence(null) // Apple HealthKit에서는 cadence를 별도로 제공하지 않음
                .averageHeartRate(averageHeartRate)
                .maxHeartRate(maxHeartRate)
                .averagePower(null) // Apple HealthKit에서는 파워를 별도로 제공하지 않음
                .maxPower(null) // Apple HealthKit에서는 파워를 별도로 제공하지 않음
                .calories(request.calories()) // Apple HealthKit에서 제공하는 칼로리 정보
                .build();
    }

    /**
     * Apple HealthKit 운동 데이터로부터 GPS 로그 생성
     */
    private List<ActivityGpsLog> createActivityGpsLogsFromAppleWorkout(Activity activity, AppleWorkoutImportRequest request) {
        List<ActivityGpsLog> gpsLogs = new ArrayList<>();

        // 위치 데이터 처리
        if (request.locationData() != null && !request.locationData().isEmpty()) {
            for (AppleWorkoutImportRequest.LocationData locationData : request.locationData()) {
                ActivityGpsLog gpsLog = ActivityGpsLog.builder()
                        .activity(activity)
                        .logTime(locationData.timestamp())
                        .latitude(locationData.latitude())
                        .longitude(locationData.longitude())
                        .elevation(locationData.altitude())
                        .speed(locationData.speed())
                        .distance(null) // Apple HealthKit에서는 누적 거리를 별도로 제공하지 않음
                        .build();
                gpsLogs.add(gpsLog);
            }
        }

        // 심박수 데이터를 GPS 로그에 병합
        if (request.heartRateData() != null && !request.heartRateData().isEmpty()) {
            mergeHeartRateData(gpsLogs, request.heartRateData());
        }

        return gpsLogs;
    }

    /**
     * 심박수 데이터를 GPS 로그에 병합
     */
    private void mergeHeartRateData(List<ActivityGpsLog> gpsLogs, List<AppleWorkoutImportRequest.HeartRateSample> heartRateData) {
        for (ActivityGpsLog gpsLog : gpsLogs) {
            // 가장 가까운 시간의 심박수 데이터 찾기
            AppleWorkoutImportRequest.HeartRateSample closestHeartRate = findClosestHeartRateSample(
                    gpsLog.getLogTime(), heartRateData);
            if (closestHeartRate != null) {
                gpsLog.updateHeartRate(closestHeartRate.heartRate().doubleValue());
            }
        }
    }

    /**
     * 가장 가까운 시간의 심박수 샘플 찾기
     */
    private AppleWorkoutImportRequest.HeartRateSample findClosestHeartRateSample(
            LocalDateTime targetTime, List<AppleWorkoutImportRequest.HeartRateSample> heartRateData) {
        return heartRateData.stream()
                .min((a, b) -> {
                    long diffA = Math.abs(java.time.Duration.between(targetTime, a.timestamp()).toSeconds());
                    long diffB = Math.abs(java.time.Duration.between(targetTime, b.timestamp()).toSeconds());
                    return Long.compare(diffA, diffB);
                })
                .orElse(null);
    }

}
