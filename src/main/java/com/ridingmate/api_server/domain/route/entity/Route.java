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
import java.util.UUID;

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
    private UUID routeId;

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
                  UUID routeId, LandscapeType landscapeType, String gpxFilePath, Double maxLat, Double maxLon,
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

    /**
     * 경로 제목 업데이트
     */
    public void updateTitle(String title) {
        this.title = title;
    }

    /**
     * 경로 설명 업데이트
     */
    public void updateDescription(String description) {
        this.description = description;
    }

    /**
     * 경로 개인정보 마스킹 및 삭제 처리
     * - 모든 개인정보 관련 필드 마스킹/제거
     */
    public void maskPersonalDataForDeletion() {
        // 1. 제목 마스킹 (개인정보 보호)
        this.title = "탈퇴한 사용자의 경로";
        
        // 2. 설명 제거 (개인정보 보호)
        this.description = null;
        
        // 3. 썸네일 이미지 경로 제거 (개인정보 보호)
        this.thumbnailImagePath = null;
        
        // 4. GPX 파일 경로 제거 (개인정보 보호)
        this.gpxFilePath = null;
        
        // 5. 거리/시간/고도 데이터 마스킹 (개인정보 보호)
        this.distance = 0.0;
        this.duration = Duration.ZERO;
        this.elevationGain = 0.0;
        
        // 6. 좌표 정보 마스킹 (개인정보 보호)
        this.maxLat = null;
        this.maxLon = null;
        this.minLat = null;
        this.minLon = null;
        this.routeLine = null;
    }
}