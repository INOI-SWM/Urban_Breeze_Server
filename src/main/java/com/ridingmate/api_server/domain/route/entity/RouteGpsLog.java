package com.ridingmate.api_server.domain.route.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "route_gps_logs", indexes = {
        @Index(name = "idx_route_gps_logs_route_id_log_time", columnList = "route_id, log_time")
})
public class RouteGpsLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(name = "log_time", nullable = false)
    private LocalDateTime logTime;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "elevation")
    private Double elevation;

    @Builder
    private RouteGpsLog(Route route, Double longitude, Double latitude, Double elevation, LocalDateTime logTime){
        this.route = route;
        this.longitude = longitude;
        this.latitude = latitude;
        this.elevation = elevation;
        this.logTime = logTime;
    }
}
