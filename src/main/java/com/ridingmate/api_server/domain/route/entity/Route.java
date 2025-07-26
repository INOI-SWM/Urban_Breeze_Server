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

    @Column(name = "region")
    private String region;

    @Column(name = "difficulty")
    private String difficulty;

    @Column(name = "thumbnail_image_path")
    private String thumbnailImagePath;

    @OneToOne(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private RouteGeometry routeGeometry;

    @OneToOne(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Recommendation recommendation;


    @Builder
    private Route(User user, String title, String description, Double distance, Duration duration, Double elevationGain,
                  String shareId, String region, String difficulty, String thumbnailImagePath) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.distance = distance;
        this.duration = duration;
        this.elevationGain = elevationGain;
        this.shareId = shareId;
        this.region = region;
        this.difficulty = difficulty;
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

    public void updateThumbnailImagePath(String thumbnailImagePath) {
        this.thumbnailImagePath = thumbnailImagePath;
    }

    public void setRouteGeometry(RouteGeometry routeGeometry) {
        this.routeGeometry = routeGeometry;
    }
}