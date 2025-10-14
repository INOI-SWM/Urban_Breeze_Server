package com.ridingmate.api_server.domain.route.dto;

import com.ridingmate.api_server.domain.route.dto.response.RecommendationListResponse;
import com.ridingmate.api_server.domain.route.dto.response.RouteListItemResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 필터링 범위 설정을 위한 최대/최소값 정보
 */
@Schema(description = "필터링 범위 정보")
public record FilterRangeInfo(
        @Schema(description = "최소 거리 (km)", example = "1.0")
        Double minDistance,
        
        @Schema(description = "최대 거리 (km)", example = "50.0")
        Double maxDistance,

        @Schema(description = "최소 상승고도 (m)", example = "0.0")
        Double minElevationGain,
        
        @Schema(description = "최대 상승고도 (m)", example = "1200.0")
        Double maxElevationGain
) {
    /**
     * FilterRangeInfo 생성 팩토리 메서드
     */
    public static FilterRangeInfo of(Double minDistance, Double maxDistance, 
                                   Double minElevationGain, Double maxElevationGain) {
        return new FilterRangeInfo(minDistance, maxDistance, minElevationGain, maxElevationGain);
    }
}
