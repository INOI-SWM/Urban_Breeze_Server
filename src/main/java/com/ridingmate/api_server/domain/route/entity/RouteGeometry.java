package com.ridingmate.api_server.domain.route.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.LineString;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "route_geometry")
public class RouteGeometry {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "route_id")
    private Route route;

    @Column(name = "gpx_file_path")
    private String gpxFilePath;

    @Column(name = "max_lat", nullable = false)
    private double maxLat;

    @Column(name = "max_lon", nullable = false)
    private double maxLon;

    @Column(name = "min_lat", nullable = false)
    private double minLat;

    @Column(name = "min_lon", nullable = false)
    private double minLon;

    @Column(name = "average_gradient", nullable = false)
    private double averageGradient;

    @Column(name = "route_line", nullable = false, columnDefinition = "LineString")
    private LineString routeLine;

    @Builder
    public RouteGeometry(Long id, Route route, String gpxFilePath, double maxLat, double maxLon, double minLat, double minLon, double averageGradient, LineString routeLine) {
        this.id = id;
        this.route = route;
        this.gpxFilePath = gpxFilePath;
        this.maxLat = maxLat;
        this.maxLon = maxLon;
        this.minLat = minLat;
        this.minLon = minLon;
        this.averageGradient = averageGradient;
        this.routeLine = routeLine;
    }
} 