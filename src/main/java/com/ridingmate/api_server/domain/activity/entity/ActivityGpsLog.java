package com.ridingmate.api_server.domain.activity.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "activity_gps_logs", indexes = {
    @Index(name = "idx_activity_gps_logs_activity_id_log_time", columnList = "activity_id, log_time")
})
public class ActivityGpsLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(name = "log_time", nullable = false)
    private LocalDateTime logTime;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "elevation")
    private Double elevation;

    @Column(name = "speed")
    private Double speed;

    @Column(name = "distance")
    private Double distance;

    @Column(name = "heart_rate")
    private Double heartRate;

    @Column(name = "cadence")
    private Double cadence;

    @Column(name = "power")
    private Double power;

    @Builder
    private ActivityGpsLog(Activity activity, LocalDateTime logTime,
                           Double latitude, Double longitude, Double elevation,
                           Double speed, Double distance, Double heartRate, 
                           Double cadence, Double power
    ){
        this.activity = activity;
        this.logTime = logTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.speed = speed;
        this.distance = distance;
        this.heartRate = heartRate;
        this.cadence = cadence;
        this.power = power;
    }

    /**
     * GPS 로그 개인정보 마스킹 처리
     * - 좌표 데이터: 즉시 파기 (원본 위치)
     * - 시간 정보: 즉시 파기 (동선 복원 가능)
     * - 생체 정보: 즉시 파기 (건강/민감 성격)
     * - 성능 데이터: 즉시 파기 (개인 성능 특성)
     */
    public void maskPersonalData() {
        // 좌표 데이터 즉시 파기 (원본 위치)
        this.latitude = null;
        this.longitude = null;
        this.elevation = null;
        
        // 시간 정보 즉시 파기 (동선 복원 가능)
        this.logTime = null;
        
        // 생체 정보 즉시 파기 (건강/민감 성격)
        this.heartRate = null;
        
        // 성능 데이터 즉시 파기 (개인 성능 특성)
        this.speed = null;
        this.distance = null;
        this.cadence = null;
        this.power = null;
    }
}
