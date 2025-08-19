package com.ridingmate.api_server.domain.route.dto.request;

import com.ridingmate.api_server.domain.route.enums.Difficulty;
import com.ridingmate.api_server.domain.route.enums.RecommendationSortType;
import com.ridingmate.api_server.domain.route.enums.RecommendationType;
import com.ridingmate.api_server.domain.route.enums.Region;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springdoc.core.annotations.ParameterObject;

import java.util.List;

@ParameterObject
public record RecommendationListRequest(
    @Parameter(
            description = "페이지 번호 (기본 값: 0)",
            example = "0",
            schema = @Schema(type = "integer", defaultValue = "0")
    )
    int page,
    
    @Parameter(
            description = "페이지 크기 (기본 값: 10)",
            example = "10",
            schema = @Schema(type = "integer", defaultValue = "10")
    )
    int size,
    
    @Parameter(
            description = "정렬 타입 (기본 값: 가까운 순)",
            example = "NEAREST",
            schema = @Schema(defaultValue = "NEAREST")
    )
    RecommendationSortType sortType,
    
    @Parameter(
            description = "추천 타입 필터 (기본 값: 전체 선택 / 다중 선택 가능)",
            example = "CROSS_COUNTRY,COMPETITION,FAMOUS"
    )
    List<RecommendationType> recommendationTypes,
    
    @Parameter(
            description = "지역 필터 (기본 값: 전체 선택 / 다중 선택 가능)",
            example = "SEOUL,GANGWON"
    )
    List<Region> regions,
    
    @Parameter(
            description = "최소 거리 (km)",
            example = "0.0",
            required = true
    )
    Double minDistanceKm,
    
    @Parameter(
            description = "최대 거리 (km)",
            example = "200.0",
            required = true
    )
    Double maxDistanceKm,
    
    @Parameter(
            description = "최소 고도 상승 (미터)",
            example = "0.0",
            required = true
    )
    Double minElevationGain,
    
    @Parameter(
            description = "최대 고도 상승 (미터)",
            example = "1000.0",
            required = true
    )
    Double maxElevationGain,
    
    @Parameter(
            description = "난이도 필터 (기본 값: 전체 선택 / 다중 선택 가능)",
            example = "EASY,MEDIUM"
    )
    List<Difficulty> difficulties,
    
    @Parameter(
            description = "사용자 현재 위치 경도 (가까운 순 정렬시 필요)",
            example = "127.0"
    )
    Double userLon,
    
    @Parameter(
            description = "사용자 현재 위치 위도 (가까운 순 정렬시 필요)",
            example = "37.5"
    )
    Double userLat
) {
    
    /**
     * 최소 거리를 미터 단위로 변환하여 반환
     * @return 최소 거리 (미터)
     */
    public Double getMinDistanceInMeter() {
        return minDistanceKm != null ? minDistanceKm * 1000 : null;
    }
    
    /**
     * 최대 거리를 미터 단위로 변환하여 반환
     * @return 최대 거리 (미터)
     */
    public Double getMaxDistanceInMeter() {
        return maxDistanceKm != null ? maxDistanceKm * 1000 : null;
    }
} 