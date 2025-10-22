package com.ridingmate.api_server.domain.privacy.service;

import com.ridingmate.api_server.domain.activity.entity.Activity;
import com.ridingmate.api_server.domain.activity.repository.ActivityRepository;
import com.ridingmate.api_server.domain.privacy.entity.LocationDataAccessLog;
import com.ridingmate.api_server.domain.privacy.enums.LocationAccessType;
import com.ridingmate.api_server.domain.privacy.repository.LocationDataAccessLogRepository;
import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.route.repository.RouteRepository;
import com.ridingmate.api_server.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 위치정보 조회 기록 관리 서비스
 * 위치정보보호법 제16조에 따른 기록 보존 및 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LocationDataAccessLogService {

    private final LocationDataAccessLogRepository locationDataAccessLogRepository;
    private final ActivityRepository activityRepository;
    private final RouteRepository routeRepository;

    /**
     * 위치정보 조회 기록 생성
     * @param user 데이터 소유자
     * @param accessor 실제 접근자 (본인 접근 시 user와 동일)
     */
    public void logLocationDataAccess(User user, User accessor, LocationAccessType accessType,
                                    String ipAddress, String userAgent,
                                    String dataType, String dataId, String purpose) {
        try {
            LocationDataAccessLog accessLog = LocationDataAccessLog.createAccessLog(
                    user, accessor, accessType, ipAddress, userAgent, dataType, dataId, purpose
            );
            
            locationDataAccessLogRepository.save(accessLog);
            
            log.info("[LocationDataAccessLog] 위치정보 조회 기록 생성: userId={}, accessorId={}, accessType={}, dataType={}, dataId={}, purpose={}",
                    user.getId(), accessor != null ? accessor.getId() : null, accessType, dataType, dataId, purpose);
                    
        } catch (Exception e) {
            log.error("[LocationDataAccessLog] 위치정보 조회 기록 생성 실패: userId={}, error={}", 
                    user.getId(), e.getMessage(), e);
        }
    }

    /**
     * 경로 GPS 데이터 조회 기록
     * @param user 데이터 소유자
     * @param accessor 실제 접근자 (본인 접근 시 user와 동일)
     */
    public void logRouteGpsAccess(User user, User accessor, String routeId, String ipAddress, String userAgent) {
        logLocationDataAccess(user, accessor, LocationAccessType.ACCESS, ipAddress, userAgent,
                "ROUTE_GPS", routeId, "ROUTE_VIEW");
    }

    /**
     * 활동 GPS 데이터 조회 기록
     * @param user 데이터 소유자
     * @param accessor 실제 접근자 (본인 접근 시 user와 동일)
     */
    public void logActivityGpsAccess(User user, User accessor, String activityId, String ipAddress, String userAgent) {
        logLocationDataAccess(user, accessor, LocationAccessType.ACCESS, ipAddress, userAgent,
                "ACTIVITY_GPS", activityId, "ACTIVITY_VIEW");
    }

    /**
     * GPX 파일 다운로드 기록
     * @param user 데이터 소유자
     * @param accessor 실제 접근자 (본인 접근 시 user와 동일)
     */
    public void logGpxDownload(User user, User accessor, String fileId, String ipAddress, String userAgent) {
        logLocationDataAccess(user, accessor, LocationAccessType.DOWNLOAD, ipAddress, userAgent,
                "GPX_FILE", fileId, "GPX_DOWNLOAD");
    }

    /**
     * 위치정보 수집 기록
     */
    public void logLocationCollection(User user, String dataType, String dataId, String purpose) {
        logLocationDataAccess(user, user, LocationAccessType.COLLECTION, null, null,
                dataType, dataId, purpose);
    }

    /**
     * 제3자에게 위치정보 제공 기록 (미래 구현용)
     * 예: 경로 공유, 분석 서비스 연동 등
     */
    public void logLocationProvision(User user, String dataType, String dataId, String recipient, String purpose) {
        logLocationDataAccess(user, user, LocationAccessType.PROVISION, null, null,
                dataType, dataId, purpose + " -> " + recipient);
    }

    /**
     * 만료된 기록 삭제 (보존기간 만료)
     */
    public void deleteExpiredLogs() {
        LocalDateTime expiredDate = LocalDateTime.now().minusDays(1095); // 3년
        
        List<LocationDataAccessLog> expiredLogs = locationDataAccessLogRepository.findExpiredLogs(expiredDate);
        
        if (!expiredLogs.isEmpty()) {
            locationDataAccessLogRepository.deleteExpiredLogs(expiredDate);
            log.info("[LocationDataAccessLog] 만료된 기록 삭제 완료: count={}", expiredLogs.size());
        }
    }

    /**
     * 특정 사용자의 조회 기록 조회
     */
    @Transactional(readOnly = true)
    public List<LocationDataAccessLog> getUserAccessLogs(Long userId) {
        return locationDataAccessLogRepository.findByUserId(userId, 
                org.springframework.data.domain.Pageable.unpaged()).getContent();
    }

    /**
     * 기존 Route/Activity에 대한 위치정보 수집 로그 소급 생성
     * (마이그레이션/테스트용)
     */
    public Map<String, Object> backfillCollectionLogs() {
        log.info("========================================");
        log.info("[LocationDataAccessLog] 소급 생성 시작");
        log.info("========================================");
        
        long startTime = System.currentTimeMillis();
        int activityCount = 0;
        int routeCount = 0;
        int activityFailed = 0;
        int routeFailed = 0;
        
        // 1. Activity 수집 로그 생성
        log.info("[Activity] 위치정보 수집 로그 생성 시작");
        List<Activity> activities = activityRepository.findAll();
        log.info("[Activity] 대상: {}개", activities.size());
        
        for (Activity activity : activities) {
            try {
                // 이미 로그가 있는지 확인
                boolean exists = locationDataAccessLogRepository.existsByDataIdAndAccessType(
                        activity.getActivityId().toString(), 
                        LocationAccessType.COLLECTION
                );
                
                if (exists) {
                    log.debug("[Activity] 이미 로그 존재, 스킵: activityId={}", activity.getActivityId());
                    continue;
                }
                
                // created_at을 accessedAt으로 사용
                LocationDataAccessLog log = LocationDataAccessLog.builder()
                        .user(activity.getUser())
                        .accessor(activity.getUser())
                        .accessType(LocationAccessType.COLLECTION)
                        .accessedAt(activity.getCreatedAt())  // 생성일시를 수집일시로
                        .ipAddress(null)
                        .userAgent(null)
                        .dataType("ACTIVITY_GPS")
                        .dataId(activity.getActivityId().toString())
                        .purpose(determinePurpose(activity))
                        .build();
                
                locationDataAccessLogRepository.save(log);
                activityCount++;
                
                if (activityCount % 100 == 0) {
                    this.log.info("[Activity] 진행: {}개 처리", activityCount);
                }
                
            } catch (Exception e) {
                this.log.error("[Activity] 로그 생성 실패: activityId={}", 
                        activity.getActivityId(), e);
                activityFailed++;
            }
        }
        
        // 2. Route 수집 로그 생성
        log.info("[Route] 위치정보 수집 로그 생성 시작");
        List<Route> routes = routeRepository.findAll();
        log.info("[Route] 대상: {}개", routes.size());
        
        for (Route route : routes) {
            try {
                // 이미 로그가 있는지 확인
                boolean exists = locationDataAccessLogRepository.existsByDataIdAndAccessType(
                        route.getId().toString(), 
                        LocationAccessType.COLLECTION
                );
                
                if (exists) {
                    log.debug("[Route] 이미 로그 존재, 스킵: routeId={}", route.getId());
                    continue;
                }
                
                // created_at을 accessedAt으로 사용
                LocationDataAccessLog log = LocationDataAccessLog.builder()
                        .user(route.getUser())
                        .accessor(route.getUser())
                        .accessType(LocationAccessType.COLLECTION)
                        .accessedAt(route.getCreatedAt())  // 생성일시를 수집일시로
                        .ipAddress(null)
                        .userAgent(null)
                        .dataType("ROUTE_GPS")
                        .dataId(route.getId().toString())
                        .purpose("ROUTE_CREATION")
                        .build();
                
                locationDataAccessLogRepository.save(log);
                routeCount++;
                
                if (routeCount % 100 == 0) {
                    this.log.info("[Route] 진행: {}개 처리", routeCount);
                }
                
            } catch (Exception e) {
                this.log.error("[Route] 로그 생성 실패: routeId={}", 
                        route.getId(), e);
                routeFailed++;
            }
        }
        
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        
        log.info("========================================");
        log.info("✅ [LocationDataAccessLog] 소급 생성 완료!");
        log.info("   - Activity: {}개 성공, {}개 실패", activityCount, activityFailed);
        log.info("   - Route: {}개 성공, {}개 실패", routeCount, routeFailed);
        log.info("   - 총 소요시간: {}초", elapsed);
        log.info("========================================");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("activityLogs", activityCount);
        result.put("routeLogs", routeCount);
        result.put("activityFailed", activityFailed);
        result.put("routeFailed", routeFailed);
        result.put("totalLogs", activityCount + routeCount);
        result.put("elapsedSeconds", elapsed);
        
        return result;
    }
    
    /**
     * Activity의 수집 목적 결정
     */
    private String determinePurpose(Activity activity) {
        if (activity.getProvider() != null) {
            switch (activity.getProvider()) {
                case APPLE_HEALTH_KIT:
                    return "APPLE_HEALTHKIT_IMPORT";
                case GARMIN:
                    return "GARMIN_IMPORT";
                case SAMSUNG_HEALTH:
                    return "SAMSUNG_HEALTH_IMPORT";
                case GOOGLE_FIT:
                    return "GOOGLE_FIT_IMPORT";
                case STRAVA:
                    return "STRAVA_IMPORT";
                case SUUNTO:
                    return "SUUNTO_IMPORT";
                default:
                    return "MANUAL_CREATION";
            }
        }
        return "MANUAL_CREATION";
    }
}
