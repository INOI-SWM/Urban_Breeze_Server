package com.ridingmate.api_server.domain.route.entity;

import com.ridingmate.api_server.domain.route.enums.Difficulty;
import com.ridingmate.api_server.domain.route.enums.LandscapeType;
import com.ridingmate.api_server.domain.route.enums.Region;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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

    @Column(name = "share_id", nullable = false)
    private String shareId;

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

    @OneToOne(mappedBy = "route", cascade = CascadeType.ALL)
    private RouteGeometry routeGeometry;

    @OneToOne(mappedBy = "route", cascade = CascadeType.ALL)
    private Recommendation recommendation;


    @Builder
    private Route(User user, String title, String description, Double distance, Duration duration, Double elevationGain,
                  String shareId, LandscapeType landscapeType) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.distance = distance;
        this.duration = duration;
        this.elevationGain = elevationGain;
        this.shareId = shareId;
        this.landscapeType = landscapeType;
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

    public void setRouteGeometry(RouteGeometry routeGeometry) {
        this.routeGeometry = routeGeometry;
    }
}