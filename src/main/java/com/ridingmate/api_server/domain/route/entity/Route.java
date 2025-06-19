package com.ridingmate.api_server.domain.route.entity;

import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "routes")
public class Route extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "total_distance")
    private Float totalDistance;

    @Column(name = "total_duration")
    private Duration totalDuration;

    @Column(name = "total_elevation_gain")
    private Float totalElevationGain;

    @Column(name = "average_gradient")
    private Float averageGradient;

    @Column(name = "thumbnail_image_path")
    private String thumbnailImagePath;

    @Column(name = "gpx_file_path")
    private String gpxFilePath;
}
