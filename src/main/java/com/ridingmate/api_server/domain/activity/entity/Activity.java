package com.ridingmate.api_server.domain.activity.entity;

import com.ridingmate.api_server.domain.activity.enums.ActivityProvider;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "activities")
public class Activity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_id", unique = true, nullable = false)
    private UUID activityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title")
    private String title;

    @Column(name = "started_at", updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at",updatable = false)
    private LocalDateTime endedAt;

    /**
     * 경로 총 거리 (단위: 미터)
     */
    @Column(name = "distance")
    private Double distance;

    /**
     * 총 소요 시간 (단위: 초)
     */
    @Column(name = "duration")
    private Duration duration;

    /**
     * 총 상승 고도 (단위: 미터)
     */
    @Column(name = "elevation_gain")
    private Double elevationGain;

    /**
     * 썸네일 이미지 경로 (대표 이미지)
     */
    @Column(name = "thumbnail_image_path")
    private String thumbnailImagePath;

    /**
     * 케이던스 (분당 페달 회전수)
     */
    @Column(name = "cadence")
    private Integer cadence;

    /**
     * 평균 심박수 (bpm)
     */
    @Column(name = "average_heart_rate")
    private Integer averageHeartRate;

    /**
     * 최대 심박수 (bpm)
     */
    @Column(name = "max_heart_rate")
    private Integer maxHeartRate;

    /**
     * 평균 파워 (W)
     */
    @Column(name = "average_power")
    private Integer averagePower;

    /**
     * 최고 파워 (W)
     */
    @Column(name = "max_power")
    private Integer maxPower;

    /**
     * 소모 칼로리 (kcal)
     */
    @Column(name = "calories")
    private Double calories;

    /**
     * 운동 기록 제공자
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private ActivityProvider provider;

    /**
     * 삭제 여부 (소프트 삭제)
     */
    @Column(name = "is_delete", nullable = false)
    private Boolean isDelete = false;

    @Builder
    private Activity(User user, String title, Double distance,
                     Duration duration, Double elevationGain,
                     LocalDateTime startedAt, LocalDateTime endedAt,
                     String thumbnailImagePath, Integer cadence,
                     Integer averageHeartRate, Integer maxHeartRate,
                     Integer averagePower, Integer maxPower, Double calories,
                     ActivityProvider provider
    ){
        this.user = user;
        this.title = title;
        this.distance = distance;
        this.duration = duration;
        this.elevationGain = elevationGain;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.thumbnailImagePath = thumbnailImagePath;
        this.cadence = cadence;
        this.averageHeartRate = averageHeartRate;
        this.maxHeartRate = maxHeartRate;
        this.averagePower = averagePower;
        this.maxPower = maxPower;
        this.calories = calories;
        this.provider = provider;
        this.activityId = UUID.randomUUID();
    }

    /**
     * 썸네일 이미지 경로 업데이트
     */
    public void updateThumbnailImagePath(String thumbnailImagePath) {
        this.thumbnailImagePath = thumbnailImagePath;
    }

    /**
     * 활동 제목 업데이트
     */
    public void updateTitle(String title) {
        this.title = title;
    }

    /**
     * 소프트 삭제 처리
     */
    public void markAsDeleted() {
        this.isDelete = true;
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.isDelete;
    }

    /**
     * 활동 개인정보 마스킹 및 삭제 처리
     * - 모든 필드를 null로 마스킹
     * - 소프트 삭제 처리
     */
    public void maskPersonalDataForDeletion() {
        // 1. 모든 개인정보 필드 마스킹
        this.title = null;
        this.thumbnailImagePath = null;
        this.averageHeartRate = null;
        this.maxHeartRate = null;
        this.cadence = null;
        this.averagePower = null;
        this.maxPower = null;
        this.distance = null;
        this.duration = null;
        this.elevationGain = null;
        this.startedAt = null;
        this.endedAt = null;
        
        // 2. 소프트 삭제 처리
        this.isDelete = true;
    }
}
