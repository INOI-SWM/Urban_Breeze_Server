package com.ridingmate.api_server.domain.route.entity;

import com.ridingmate.api_server.global.util.GeometryUtil;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "route_geometries")
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

    @Column(name = "route_line", nullable = false, columnDefinition = "geometry(LineString, 4326)")
    private LineString routeLine;

    @Builder
    private RouteGeometry(Long id, Route route, String gpxFilePath, double maxLat, double maxLon, double minLat, double minLon, LineString routeLine) {
        this.id = id;
        this.route = route;
        this.gpxFilePath = gpxFilePath;
        this.maxLat = maxLat;
        this.maxLon = maxLon;
        this.minLat = minLat;
        this.minLon = minLon;
        this.routeLine = routeLine;
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