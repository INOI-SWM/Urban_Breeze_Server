package com.ridingmate.api_server.domain.activity.dto.projection;

import org.locationtech.jts.geom.Coordinate;

/**
 * GPS 로그 데이터를 위한 Projection DTO
 * 좌표와 상세 정보를 한 번에 조회하기 위해 사용
 */
public record GpsLogProjection(
    Double longitude,
    Double latitude,
    Double elevation,
    Double speed,
    Double heartRate,
    Double cadence,
    Double power
) {
    /**
     * 속도를 km/h로 변환하여 반환
     */
    public Double getSpeedInKmh() {
        return speed != null ? speed * 3.6 : null;
    }
    
    /**
     * Coordinate 객체로 변환
     */
    public Coordinate toCoordinate() {
        return new Coordinate(longitude, latitude, elevation);
    }
}
