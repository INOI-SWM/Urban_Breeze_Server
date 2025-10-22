package com.ridingmate.api_server.domain.privacy.service;

import com.ridingmate.api_server.domain.privacy.entity.LocationDataAccessLog;
import com.ridingmate.api_server.domain.privacy.enums.LocationAccessType;
import com.ridingmate.api_server.domain.privacy.repository.LocationDataAccessLogRepository;
import com.ridingmate.api_server.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
}
