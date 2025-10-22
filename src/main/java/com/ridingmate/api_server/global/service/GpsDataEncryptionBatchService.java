package com.ridingmate.api_server.global.service;

import com.ridingmate.api_server.global.util.GpsEncryptionUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * GPS 암호화 배치 처리 서비스 (트랜잭션 분리용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GpsDataEncryptionBatchService {

    @PersistenceContext
    private final EntityManager entityManager;
    
    private final GpsEncryptionUtil gpsEncryptionUtil;

    /**
     * 암호화 대상 Activity 개수 조회
     * 영문자가 없으면 평문 GPS 좌표 (예: 37.5665)
     * 영문자가 있으면 Base64 암호화된 데이터 (예: y9XAgzW3...)
     */
    @Transactional(readOnly = true)
    public long getActivityCount() {
        String countSql = 
            "SELECT COUNT(DISTINCT activity_id) FROM activity_gps_logs " +
            "WHERE latitude !~ '[a-zA-Z]'";  // 영문자가 없는 경우
        Query countQuery = entityManager.createNativeQuery(countSql);
        return ((Number) countQuery.getSingleResult()).longValue();
    }

    /**
     * 암호화 대상 Route 개수 조회
     */
    @Transactional(readOnly = true)
    public long getRouteCount() {
        String countSql = 
            "SELECT COUNT(DISTINCT route_id) FROM route_gps_logs " +
            "WHERE latitude !~ '[a-zA-Z]'";  // 영문자가 없는 경우
        Query countQuery = entityManager.createNativeQuery(countSql);
        return ((Number) countQuery.getSingleResult()).longValue();
    }

    /**
     * Activity 단위로 GPS 로그 암호화 (독립적인 트랜잭션)
     * 중간 상태(일부만 암호화)도 처리함
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int processActivityBatch(long minActivityId, int batchSize, 
                                     AtomicLong processed, AtomicLong failed) {
        
        // 암호화되지 않은 로그가 있는 Activity ID 조회 (중간 상태 포함)
        String selectActivitySql = 
            "SELECT DISTINCT activity_id FROM activity_gps_logs " +
            "WHERE activity_id > :minId AND latitude !~ '[a-zA-Z]' " +
            "ORDER BY activity_id LIMIT :limit";
        
        Query selectActivityQuery = entityManager.createNativeQuery(selectActivitySql);
        selectActivityQuery.setParameter("minId", minActivityId);
        selectActivityQuery.setParameter("limit", batchSize);
        
        @SuppressWarnings("unchecked")
        List<Long> activityIds = selectActivityQuery.getResultList();
        
        if (activityIds.isEmpty()) {
            return 0;
        }
        
        // 각 Activity의 평문 GPS 로그만 암호화 (이미 암호화된 것은 건너뜀)
        for (Long activityId : activityIds) {
            try {
                encryptGpsLogsForActivity(activityId, processed, failed);
            } catch (Exception e) {
                log.error("[activity_gps_logs] Activity 암호화 실패: activityId={}, error={}", 
                        activityId, e.getMessage(), e);
            }
        }
        
        entityManager.flush();
        entityManager.clear();
        
        return activityIds.size();
    }

    /**
     * Route 단위로 GPS 로그 암호화 (독립적인 트랜잭션)
     * 중간 상태(일부만 암호화)도 처리함
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int processRouteBatch(long minRouteId, int batchSize, 
                                  AtomicLong processed, AtomicLong failed) {
        
        // 암호화되지 않은 로그가 있는 Route ID 조회 (중간 상태 포함)
        String selectRouteSql = 
            "SELECT DISTINCT route_id FROM route_gps_logs " +
            "WHERE route_id > :minId AND latitude !~ '[a-zA-Z]' " +
            "ORDER BY route_id LIMIT :limit";
        
        Query selectRouteQuery = entityManager.createNativeQuery(selectRouteSql);
        selectRouteQuery.setParameter("minId", minRouteId);
        selectRouteQuery.setParameter("limit", batchSize);
        
        @SuppressWarnings("unchecked")
        List<Long> routeIds = selectRouteQuery.getResultList();
        
        if (routeIds.isEmpty()) {
            return 0;
        }
        
        // 각 Route의 평문 GPS 로그만 암호화 (이미 암호화된 것은 건너뜀)
        for (Long routeId : routeIds) {
            try {
                encryptGpsLogsForRoute(routeId, processed, failed);
            } catch (Exception e) {
                log.error("[route_gps_logs] Route 암호화 실패: routeId={}, error={}", 
                        routeId, e.getMessage(), e);
            }
        }
        
        entityManager.flush();
        entityManager.clear();
        
        return routeIds.size();
    }

    /**
     * 특정 Activity의 모든 GPS 로그 암호화
     */
    private void encryptGpsLogsForActivity(Long activityId, AtomicLong processed, AtomicLong failed) {
        String selectSql = 
            "SELECT id, latitude, longitude, elevation FROM activity_gps_logs " +
            "WHERE activity_id = :activityId AND latitude !~ '[a-zA-Z]'";
        
        Query selectQuery = entityManager.createNativeQuery(selectSql);
        selectQuery.setParameter("activityId", activityId);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = selectQuery.getResultList();
        
        if (results.isEmpty()) {
            return; // 암호화할 게 없으면 종료
        }
        
        long startTime = System.currentTimeMillis();
        log.debug("   → Activity {} : 암호화할 GPS 로그 {}개 발견", activityId, results.size());
        
        int localProcessed = 0;
        int localFailed = 0;
        
        for (Object[] row : results) {
            try {
                Long id = ((Number) row[0]).longValue();
                Double latitude = Double.parseDouble(String.valueOf(row[1]));
                Double longitude = Double.parseDouble(String.valueOf(row[2]));
                Double elevation = row[3] != null ? Double.parseDouble(String.valueOf(row[3])) : null;
                
                // 암호화
                String encLat = gpsEncryptionUtil.encrypt(latitude);
                String encLng = gpsEncryptionUtil.encrypt(longitude);
                String encElev = elevation != null ? gpsEncryptionUtil.encrypt(elevation) : null;
                
                // 업데이트
                String updateSql = 
                    "UPDATE activity_gps_logs SET latitude = :lat, longitude = :lng, elevation = :elev WHERE id = :id";
                
                Query updateQuery = entityManager.createNativeQuery(updateSql);
                updateQuery.setParameter("lat", encLat);
                updateQuery.setParameter("lng", encLng);
                updateQuery.setParameter("elev", encElev);
                updateQuery.setParameter("id", id);
                updateQuery.executeUpdate();
                
                processed.incrementAndGet();
                localProcessed++;
                
            } catch (Exception e) {
                failed.incrementAndGet();
                localFailed++;
                log.error("[activity_gps_logs] 레코드 암호화 실패: id={}, activityId={}, error={}", 
                        row[0], activityId, e.getMessage());
            }
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        log.info("   ✅ Activity {} : 암호화 완료 (성공: {}, 실패: {}, 소요: {}ms)", 
                activityId, localProcessed, localFailed, elapsed);
    }

    /**
     * 특정 Route의 모든 GPS 로그 암호화
     */
    private void encryptGpsLogsForRoute(Long routeId, AtomicLong processed, AtomicLong failed) {
        String selectSql = 
            "SELECT id, latitude, longitude, elevation FROM route_gps_logs " +
            "WHERE route_id = :routeId AND latitude !~ '[a-zA-Z]'";
        
        Query selectQuery = entityManager.createNativeQuery(selectSql);
        selectQuery.setParameter("routeId", routeId);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = selectQuery.getResultList();
        
        if (results.isEmpty()) {
            return; // 암호화할 게 없으면 종료
        }
        
        long startTime = System.currentTimeMillis();
        log.debug("   → Route {} : 암호화할 GPS 로그 {}개 발견", routeId, results.size());
        
        int localProcessed = 0;
        int localFailed = 0;
        
        for (Object[] row : results) {
            try {
                Long id = ((Number) row[0]).longValue();
                Double latitude = Double.parseDouble(String.valueOf(row[1]));
                Double longitude = Double.parseDouble(String.valueOf(row[2]));
                Double elevation = row[3] != null ? Double.parseDouble(String.valueOf(row[3])) : null;
                
                // 암호화
                String encLat = gpsEncryptionUtil.encrypt(latitude);
                String encLng = gpsEncryptionUtil.encrypt(longitude);
                String encElev = elevation != null ? gpsEncryptionUtil.encrypt(elevation) : null;
                
                // 업데이트
                String updateSql = 
                    "UPDATE route_gps_logs SET latitude = :lat, longitude = :lng, elevation = :elev WHERE id = :id";
                
                Query updateQuery = entityManager.createNativeQuery(updateSql);
                updateQuery.setParameter("lat", encLat);
                updateQuery.setParameter("lng", encLng);
                updateQuery.setParameter("elev", encElev);
                updateQuery.setParameter("id", id);
                updateQuery.executeUpdate();
                
                processed.incrementAndGet();
                localProcessed++;
                
            } catch (Exception e) {
                failed.incrementAndGet();
                localFailed++;
                log.error("[route_gps_logs] 레코드 암호화 실패: id={}, routeId={}, error={}", 
                        row[0], routeId, e.getMessage());
            }
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        log.info("   ✅ Route {} : 암호화 완료 (성공: {}, 실패: {}, 소요: {}ms)", 
                routeId, localProcessed, localFailed, elapsed);
    }
}

