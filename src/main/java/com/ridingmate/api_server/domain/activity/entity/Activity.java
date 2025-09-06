package com.ridingmate.api_server.domain.activity.entity;

import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "activities")
public class Activity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    private LocalDateTime endedAt;

    /**
     * 경로 총 거리 (단위: 미터)
     */
    @Column(name = "distance", nullable = false)
    private Double distance;

    /**
     * 총 소요 시간 (단위: 초)
     */
    @Column(name = "duration", nullable = false)
    private Duration duration;

    /**
     * 총 상승 고도 (단위: 미터)
     */
    @Column(name = "elevation_gain", nullable = false)
    private Double elevationGain;

    @Builder
    private Activity(User user, String title, Double distance,
                     Duration duration, Double elevationGain,
                     LocalDateTime startedAt, LocalDateTime endedAt
    ){
        this.user = user;
        this.title = title;
        this.distance = distance;
        this.duration = duration;
        this.elevationGain = elevationGain;
        this.startedAt = startedAt;
        this.endedAt = endedAt;

    }
}
