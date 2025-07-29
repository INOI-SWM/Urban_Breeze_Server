package com.ridingmate.api_server.domain.activity.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "activity_performances")
public class ActivityPerformance {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "activity_id")
    private Activity activity;

    @Column(name = "average_speed")
    private Double averageSpeed;

    @Column(name = "max_speed")
    private Double maxSpeed;

    @Column(name = "moving_time")
    private Duration movingTime;

    @Column(name = "elevation_loss")
    private Double elevationLoss;

    @Column(name = "average_cadence")
    private Double averageCadence;

    @Column(name = "max_cadence")
    private Double maxCadence;

    @Column(name = "average_heart_rate")
    private Double averageHeartRate;

    @Column(name = "average_power")
    private Double averagePower;

    @Column(name = "max_power")
    private Double maxPower;
}
