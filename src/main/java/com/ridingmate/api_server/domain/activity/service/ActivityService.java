package com.ridingmate.api_server.domain.activity.service;

import com.ridingmate.api_server.domain.activity.dto.projection.ActivityDateRangeProjection;
import com.ridingmate.api_server.domain.activity.dto.projection.ActivityStatsProjection;
import com.ridingmate.api_server.domain.activity.dto.request.ActivityListRequest;
import com.ridingmate.api_server.domain.activity.dto.request.ActivityStatsRequest;
import com.ridingmate.api_server.domain.activity.dto.response.ActivityStatsResponse;
import com.ridingmate.api_server.domain.activity.entity.Activity;
import com.ridingmate.api_server.domain.activity.entity.ActivityImage;
import com.ridingmate.api_server.domain.activity.enums.ActivityStatsPeriod;
import com.ridingmate.api_server.domain.activity.exception.ActivityException;
import com.ridingmate.api_server.domain.activity.exception.code.ActivityCommonErrorCode;
import com.ridingmate.api_server.domain.activity.repository.ActivityGpsLogRepository;
import com.ridingmate.api_server.domain.activity.repository.ActivityImageRepository;
import com.ridingmate.api_server.domain.activity.repository.ActivityRepository;
import com.ridingmate.api_server.domain.auth.exception.AuthErrorCode;
import com.ridingmate.api_server.domain.auth.exception.AuthException;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.repository.UserRepository;
import com.ridingmate.api_server.infra.terra.dto.response.TerraPayload;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityGpsLogRepository activityGpsLogRepository;
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
     * 사용자의 활동 통계 조회
     * @param userId 사용자 ID
     * @param request 통계 요청
     * @return 활동 통계 응답
     */
    @Transactional(readOnly = true)
    public ActivityStatsResponse getActivityStats(Long userId, ActivityStatsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.AUTHENTICATION_USER_NOT_FOUND));


        // 전체 통계 조회
        ActivityStatsProjection overallStatsData = activityRepository.findOverallActivityStats(user.getId());
        ActivityStatsResponse.OverallStats overallStats = createOverallStats(user, overallStatsData);

        if (overallStats.totalActivityCount() == 0) {
            // 활동이 없는 경우 빈 통계 반환
            return createEmptyStats(request.period(), request.targetDate());
        }

        // 선택된 기간의 통계
        ActivityStatsResponse.CurrentPeriodStats currentPeriod = getCurrentPeriodStats(user, request.period(), request.targetDate());

        // 차트 데이터 생성 (선택된 기간의 세부 통계)
        List<ActivityStatsResponse.PeriodStats> chartData = generateDetailedChartData(user, request.period(), request.targetDate());

        return new ActivityStatsResponse(
                request.period(),
                currentPeriod,
                chartData,
                overallStats
        );
    }

    private ActivityStatsResponse createEmptyStats(ActivityStatsPeriod period, LocalDate targetDate) {
        return new ActivityStatsResponse(
                period,
                new ActivityStatsResponse.CurrentPeriodStats(
                        getCurrentPeriodLabel(period, targetDate),
                        0.0, 0.0, 0L, 0
                ),
                List.of(),
                new ActivityStatsResponse.OverallStats(
                        0.0, 0.0, 0L, 0, targetDate, targetDate
                )
        );
    }

    private ActivityStatsResponse.OverallStats createOverallStats(User user, ActivityStatsProjection statsData) {
        ActivityDateRangeProjection dateRange = activityRepository.findFirstAndLastActivityDate(user);
        
        return new ActivityStatsResponse.OverallStats(
                statsData.getTotalDistanceInKm(), // 이미 킬로미터로 변환됨
                statsData.totalElevation(),
                statsData.getTotalDurationSeconds(), // 초 단위로 변경
                statsData.count().intValue(),
                dateRange.firstActivityDate() != null ? dateRange.firstActivityDate().toLocalDate() : LocalDate.now(),
                dateRange.lastActivityDate() != null ? dateRange.lastActivityDate().toLocalDate() : LocalDate.now()
        );
    }

    private ActivityStatsResponse.CurrentPeriodStats getCurrentPeriodStats(User user, ActivityStatsPeriod period, LocalDate targetDate) {
        LocalDateTime[] periodRange = getPeriodRange(period, targetDate);
        ActivityStatsProjection statsData = activityRepository.findActivityStatsByPeriod(user.getId(), periodRange[0], periodRange[1]);

        return new ActivityStatsResponse.CurrentPeriodStats(
                getCurrentPeriodLabel(period, targetDate),
                statsData.getTotalDistanceInKm(), // 이미 킬로미터로 변환됨
                statsData.totalElevation(),
                statsData.getTotalDurationSeconds(), // 초 단위로 변경
                statsData.count().intValue()
        );
    }

    /**
     * 선택된 기간의 세부 통계 생성
     * WEEK: 해당 주의 일~토 요일별 통계
     * MONTH: 해당 월의 1일~말일 일별 통계  
     * YEAR: 해당 년의 1월~12월 월별 통계
     */
    private List<ActivityStatsResponse.PeriodStats> generateDetailedChartData(User user, ActivityStatsPeriod period, LocalDate targetDate) {
        return switch (period) {
            case WEEK -> generateWeeklyDetailData(user, targetDate);
            case MONTH -> generateMonthlyDetailData(user, targetDate);
            case YEAR -> generateYearlyDetailData(user, targetDate);
        };
    }

    /**
     * 해당 주의 일~토 요일별 통계 생성
     */
    private List<ActivityStatsResponse.PeriodStats> generateWeeklyDetailData(User user, LocalDate targetDate) {
        List<ActivityStatsResponse.PeriodStats> chartData = new ArrayList<>();
        
        // 일요일부터 시작하는 주 계산
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        LocalDate weekStart = targetDate.with(weekFields.dayOfWeek(), 1); // 일요일
        
        // 일~토 각 요일별 통계
        for (int i = 0; i < 7; i++) {
            LocalDate dayDate = weekStart.plusDays(i);
            LocalDateTime dayStart = dayDate.atStartOfDay();
            LocalDateTime dayEnd = dayDate.plusDays(1).atStartOfDay();
            
            ActivityStatsProjection statsData = activityRepository.findActivityStatsByPeriod(
                user.getId(), dayStart, dayEnd);
            
            String dayLabel = getDayOfWeekLabel(dayDate.getDayOfWeek());
            
            chartData.add(new ActivityStatsResponse.PeriodStats(
                    dayLabel,
                    dayDate,
                    dayDate,
                    statsData.getTotalDistanceInKm(),
                    statsData.totalElevation(),
                    statsData.getTotalDurationSeconds(),
                    statsData.count().intValue()
            ));
        }
        
        return chartData;
    }

    /**
     * 해당 월의 1일~말일 일별 통계 생성
     */
    private List<ActivityStatsResponse.PeriodStats> generateMonthlyDetailData(User user, LocalDate targetDate) {
        List<ActivityStatsResponse.PeriodStats> chartData = new ArrayList<>();
        
        LocalDate monthStart = targetDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate monthEnd = targetDate.with(TemporalAdjusters.lastDayOfMonth());
        
        LocalDate current = monthStart;
        while (!current.isAfter(monthEnd)) {
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd = current.plusDays(1).atStartOfDay();
            
            ActivityStatsProjection statsData = activityRepository.findActivityStatsByPeriod(
                user.getId(), dayStart, dayEnd);
            
            chartData.add(new ActivityStatsResponse.PeriodStats(
                    String.valueOf(current.getDayOfMonth()),
                    current,
                    current,
                    statsData.getTotalDistanceInKm(),
                    statsData.totalElevation(),
                    statsData.getTotalDurationSeconds(),
                    statsData.count().intValue()
            ));
            
            current = current.plusDays(1);
        }
        
        return chartData;
    }

    /**
     * 해당 년의 1월~12월 월별 통계 생성
     */
    private List<ActivityStatsResponse.PeriodStats> generateYearlyDetailData(User user, LocalDate targetDate) {
        List<ActivityStatsResponse.PeriodStats> chartData = new ArrayList<>();
        
        int year = targetDate.getYear();
        
        for (int month = 1; month <= 12; month++) {
            LocalDate monthStart = LocalDate.of(year, month, 1);
            LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());
            
            ActivityStatsProjection statsData = activityRepository.findActivityStatsByPeriod(
                user.getId(), monthStart.atStartOfDay(), monthEnd.plusDays(1).atStartOfDay());
            
            chartData.add(new ActivityStatsResponse.PeriodStats(
                    String.valueOf(month),
                    monthStart,
                    monthEnd,
                    statsData.getTotalDistanceInKm(),
                    statsData.totalElevation(),
                    statsData.getTotalDurationSeconds(),
                    statsData.count().intValue()
            ));
        }
        
        return chartData;
    }

    /**
     * 요일 라벨 반환 (일~토 순서)
     */
    private String getDayOfWeekLabel(java.time.DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case SUNDAY -> "일";
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
        };
    }

    private LocalDateTime[] getPeriodRange(ActivityStatsPeriod period, LocalDate date) {
        return switch (period) {
            case WEEK -> {
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                LocalDate weekStart = date.with(weekFields.dayOfWeek(), 1); // 일요일부터 시작
                LocalDate weekEnd = weekStart.plusDays(7);
                yield new LocalDateTime[]{weekStart.atStartOfDay(), weekEnd.atStartOfDay()};
            }
            case MONTH -> {
                LocalDate monthStart = date.with(TemporalAdjusters.firstDayOfMonth());
                LocalDate monthEnd = date.with(TemporalAdjusters.firstDayOfNextMonth());
                yield new LocalDateTime[]{monthStart.atStartOfDay(), monthEnd.atStartOfDay()};
            }
            case YEAR -> {
                LocalDate yearStart = date.with(TemporalAdjusters.firstDayOfYear());
                LocalDate yearEnd = date.with(TemporalAdjusters.firstDayOfNextYear());
                yield new LocalDateTime[]{yearStart.atStartOfDay(), yearEnd.atStartOfDay()};
            }
        };
    }

    private String getCurrentPeriodLabel(ActivityStatsPeriod period, LocalDate date) {
        return switch (period) {
            case WEEK -> {
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                int weekOfYear = date.get(weekFields.weekOfWeekBasedYear());
                yield date.getYear() % 100 + "년 " + weekOfYear + "주";
            }
            case MONTH -> date.getYear() % 100 + "년 " + date.getMonthValue() + "월";
            case YEAR -> date.getYear() + "년";
        };
    }

    private String getPeriodLabel(ActivityStatsPeriod period, LocalDate date) {
        return switch (period) {
            case WEEK -> {
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                int weekOfYear = date.get(weekFields.weekOfWeekBasedYear());
                yield String.valueOf(weekOfYear);
            }
            case MONTH -> String.valueOf(date.getMonthValue());
            case YEAR -> String.valueOf(date.getYear());
        };
    }

    private LocalDate getNextPeriodStart(ActivityStatsPeriod period, LocalDate current) {
        return switch (period) {
            case WEEK -> current.plusWeeks(1);
            case MONTH -> current.plusMonths(1);
            case YEAR -> current.plusYears(1);
        };
    }

}
