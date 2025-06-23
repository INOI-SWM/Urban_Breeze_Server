package com.ridingmate.api_server.domain.route.entity;

import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.LineString;

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

    @Column(name = "route_line", columnDefinition = "geometry(LineString, 4326)", nullable = false)
    private LineString routeLine;

    @Column(name = "total_distance", nullable = false)
    private Double totalDistance;

    @Column(name = "total_duration", nullable = false)
    private Duration totalDuration;

    @Column(name = "total_elevation_gain", nullable = false)
    private Double totalElevationGain;

    @Column(name = "average_gradient", nullable = false)
    private Double averageGradient;

    @Column(name = "thumbnail_image_path")
    private String thumbnailImagePath;

    @Column(name = "gpx_file_path")
    private String gpxFilePath;

    @Builder
    private Route(User user, String name, LineString routeLine, Double totalDistance,
                  Duration totalDuration, Double totalElevationGain, Double averageGradient,
                  String thumbnailImagePath, String gpxFilePath) {
        this.user = user;
        this.name = name;
        this.routeLine = routeLine;
        this.totalDistance = totalDistance;
        this.totalDuration = totalDuration;
        this.totalElevationGain = totalElevationGain;
        this.averageGradient = averageGradient;
    }
}