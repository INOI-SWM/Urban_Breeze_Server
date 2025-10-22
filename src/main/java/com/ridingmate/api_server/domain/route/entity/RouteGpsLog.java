package com.ridingmate.api_server.domain.route.entity;

import com.ridingmate.api_server.domain.route.exception.RouteException;
import com.ridingmate.api_server.domain.route.exception.code.RouteCreationErrorCode;
import com.ridingmate.api_server.global.config.EncryptedDoubleConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "route_gps_logs", indexes = {
        @Index(name = "idx_route_gps_logs_route_id_log_time", columnList = "route_id, log_time")
})
public class RouteGpsLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(name = "log_time", nullable = false)
    private LocalDateTime logTime;

    @Convert(converter = EncryptedDoubleConverter.class)
    @Column(name = "longitude", nullable = false, columnDefinition = "TEXT")
    private Double longitude;

    @Convert(converter = EncryptedDoubleConverter.class)
    @Column(name = "latitude", nullable = false, columnDefinition = "TEXT")
    private Double latitude;

    @Convert(converter = EncryptedDoubleConverter.class)
    @Column(name = "elevation", columnDefinition = "TEXT")
    private Double elevation;

    @Builder
    private RouteGpsLog(Route route, Double longitude, Double latitude, Double elevation, LocalDateTime logTime){
        // GPS 로그 필수 값 검증
        if (latitude == null || longitude == null || logTime == null) {
            throw new RouteException(RouteCreationErrorCode.INVALID_GPS_LOG_COORDINATES);
        }
        
        this.route = route;
        this.longitude = longitude;
        this.latitude = latitude;
        this.elevation = elevation;
        this.logTime = logTime;
    }

    /**
     * 경로 GPS 로그 개인정보 마스킹 처리
     * - GPS 데이터는 법정 보존 대상이지만 개인정보는 마스킹
     * - 좌표, 고도, 시간 정보는 개인정보로 간주하여 마스킹
     */
    public void maskPersonalDataForDeletion() {
        // 1. 좌표 정보 마스킹 (개인 위치 정보)
        this.latitude = null;
        this.longitude = null;
        
        // 2. 고도 정보 마스킹 (개인 위치 정보)
        this.elevation = null;
        
        // 3. 시간 정보 마스킹 (개인 활동 패턴)
        this.logTime = null;
        
        // GPS 데이터는 법정 보존 대상이지만 개인정보 보호를 위해 마스킹
        // - 통계용 집계 데이터는 별도 테이블에서 관리
        // - 개별 GPS 로그는 개인정보로 간주하여 마스킹
    }
}
