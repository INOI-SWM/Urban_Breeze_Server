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

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "share_id", nullable = false)
    private String shareId;

    @Column(name = "route_line", columnDefinition = "geometry(LineString, 4326)", nullable = false)
    private LineString routeLine;

    /**
     * 경로 총 거리 (단위: 미터)
     */
    @Column(name = "total_distance", nullable = false)
    private Double totalDistance;

    /**
     * 총 소요 시간 (단위: 초)
     */
    @Column(name = "total_duration", nullable = false)
    private Duration totalDuration;

    /**
     * 총 상승 고도 (단위: 미터)
     */
    @Column(name = "total_elevation_gain", nullable = false)
    private Double totalElevationGain;

    /**
     * 평균 경사도 (단위: 퍼센트)
     */
    @Column(name = "average_gradient", nullable = false)
    private Double averageGradient;

    @Column(name = "thumbnail_image_path")
    private String thumbnailImagePath;

    /**
     * 경로 영역 최소 위도
     */
    @Column(name = "minLat", nullable = false)
    private Double minLat;

    /**
     * 경로 영역 최소 경도
     */
    @Column(name = "minLon", nullable = false)
    private Double minLon;

    /**
     * 경로 영역 최대 위도
     */
    @Column(name = "maxLat", nullable = false)
    private Double maxLat;

    /**
     * 경로 영역 최대 경도
     */
    @Column(name = "maxLon", nullable = false)
    private Double maxLon;

    @Column(name = "gpx_file_path")
    private String gpxFilePath;

    @Builder
    private Route(User user, String title, LineString routeLine, String shareId,
                  Double totalDistance, Duration totalDuration, Double totalElevationGain, Double averageGradient,
                  Double minLon, Double minLat, Double maxLon, Double maxLat,
                  String thumbnailImagePath, String gpxFilePath) {
        this.user = user;
        this.title = title;
        this.shareId = shareId;
        this.routeLine = routeLine;
        this.totalDistance = totalDistance;
        this.totalDuration = totalDuration;
        this.totalElevationGain = totalElevationGain;
        this.minLon = minLon;
        this.minLat = minLat;
        this.maxLon = maxLon;
        this.maxLat = maxLat;
        this.averageGradient = averageGradient;
    }

    /**
     * 총 거리를 km 단위로 변환해서 반환 (소수점 2자리)
     * @return 거리 (km)
     */
    public double getDistanceInKm() {
        return new java.math.BigDecimal(this.totalDistance / 1000.0)
                .setScale(2, java.math.RoundingMode.DOWN)
                .doubleValue();
    }

    public void updateThumbnailImagePath(String thumbnailImagePath) {
        this.thumbnailImagePath = thumbnailImagePath;
    }
}