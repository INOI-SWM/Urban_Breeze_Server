package com.ridingmate.api_server.domain.activity.dto.projection;

/**
 * 활동 통계 집계를 위한 Projection DTO
 * Native Query 결과를 타입 안전하게 받기 위해 사용
 */
public record ActivityStatsProjection(
        Long count,
        Double totalDistance,
        Double totalElevation,
        Long totalDurationSeconds
) {
    /**
     * 거리를 킬로미터로 변환하여 반환
     * @return 킬로미터 단위 거리
     */
    public Double getTotalDistanceInKm() {
        return totalDistance != null ? totalDistance / 1000.0 : 0.0;
    }
    
    /**
     * 초 단위 시간 반환
     * @return 초 단위 시간
     */
    public Long getTotalDurationSeconds() {
        return totalDurationSeconds != null ? totalDurationSeconds : 0L;
    }
}
