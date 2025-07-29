package com.ridingmate.api_server.domain.activity.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "activity_gps_logs")
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
    private Duration distance;

    @Column(name = "heart_rate")
    private Double heartRate;

    @Column(name = "cadence")
    private Double cadence;
}
