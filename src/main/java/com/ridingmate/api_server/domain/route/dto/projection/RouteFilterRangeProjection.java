package com.ridingmate.api_server.domain.route.dto.projection;

/**
 * 경로 필터링 범위 정보를 위한 Projection DTO
 */
public record RouteFilterRangeProjection(
    Double minDistance,
    Double maxDistance,
    Double minElevationGain,
    Double maxElevationGain
) {
    /**
     * 최소 거리를 킬로미터로 변환하여 반환
     */
    public Double getMinDistanceInKm() {
        return minDistance != null ? minDistance / 1000.0 : null;
    }
    
    /**
     * 최대 거리를 킬로미터로 변환하여 반환
     */
    public Double getMaxDistanceInKm() {
        return maxDistance != null ? maxDistance / 1000.0 : null;
    }
    
    /**
     * 최소 상승 고도를 반올림하여 반환
     */
    public Double getRoundedMinElevationGain() {
        return minElevationGain != null ? Math.round(minElevationGain * 10.0) / 10.0 : null;
    }
    
    /**
     * 최대 상승 고도를 반올림하여 반환
     */
    public Double getRoundedMaxElevationGain() {
        return maxElevationGain != null ? Math.round(maxElevationGain * 10.0) / 10.0 : null;
    }
}
