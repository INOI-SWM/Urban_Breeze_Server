package com.ridingmate.api_server.domain.route.entity;

import com.ridingmate.api_server.domain.route.enums.Difficulty;
import com.ridingmate.api_server.domain.route.enums.LandscapeType;
import com.ridingmate.api_server.domain.route.enums.Region;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.global.entity.BaseTimeEntity;
import com.ridingmate.api_server.global.util.GeometryUtil;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import java.time.Duration;
import java.util.List;

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

    @Column(name = "description")
    private String description;

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

    @Column(name = "route_id", nullable = false, unique = true)
    private String routeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "region")
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "landscape_type")
    private LandscapeType landscapeType;

    @Column(name = "thumbnail_image_path")
    private String thumbnailImagePath;

    @Column(name = "gpx_file_path")
    private String gpxFilePath;

    @Column(name = "max_lat", nullable = false)
    private Double maxLat;

    @Column(name = "max_lon", nullable = false)
    private Double maxLon;

    @Column(name = "min_lat", nullable = false)
    private Double minLat;

    @Column(name = "min_lon", nullable = false)
    private Double minLon;

    @Column(name = "route_line", nullable = false, columnDefinition = "geometry(LineString, 4326)")
    private LineString routeLine;

    @OneToOne(mappedBy = "route", cascade = CascadeType.ALL)
    private Recommendation recommendation;


    @Builder
    private Route(User user, String title, String description, Double distance, Duration duration, Double elevationGain,
                  String routeId, LandscapeType landscapeType, String gpxFilePath, Double maxLat, Double maxLon, 
                  Double minLat, Double minLon, LineString routeLine) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.distance = distance;
        this.duration = duration;
        this.elevationGain = elevationGain;
        this.routeId = routeId;
        this.landscapeType = landscapeType;
        this.gpxFilePath = gpxFilePath;
        this.maxLat = maxLat;
        this.maxLon = maxLon;
        this.minLat = minLat;
        this.minLon = minLon;
        this.routeLine = routeLine;
    }

    /**
     * 총 거리를 km 단위로 변환해서 반환 (소수점 2자리)
     * @return 거리 (km)
     */
    public double getDistanceInKm() {
        return new java.math.BigDecimal(this.distance / 1000.0)
                .setScale(2, java.math.RoundingMode.DOWN)
                .doubleValue();
    }

    /**
     * 총 상승 고도를 소수 둘째자리에서 반올림해서 반환
     * @return 상승 고도 (m)
     */
    public double getRoundedElevationGain() {
        return Math.round(this.elevationGain * 100.0) / 100.0;
    }

    public void updateThumbnailImagePath(String thumbnailImagePath) {
        this.thumbnailImagePath = thumbnailImagePath;
    }

    public void updateGpxFilePath(String gpxFilePath) {
        this.gpxFilePath = gpxFilePath;
    }

    /**
     * 출발 좌표 반환
     */
    public Coordinate getStartCoordinate() {
        return GeometryUtil.getStartCoordinate(this.routeLine);
    }

    /**
     * 도착 좌표 반환
     */
    public Coordinate getEndCoordinate() {
        return GeometryUtil.getEndCoordinate(this.routeLine);
    }

    /**
     * 모든 좌표 반환
     */
    public List<Coordinate> getAllCoordinates() {
        return GeometryUtil.getAllCoordinates(this.routeLine);
    }
}