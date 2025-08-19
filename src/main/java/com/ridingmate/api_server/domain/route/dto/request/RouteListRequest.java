package com.ridingmate.api_server.domain.route.dto.request;

import com.ridingmate.api_server.domain.route.enums.RouteRelationType;
import com.ridingmate.api_server.domain.route.enums.RouteSortType;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springdoc.core.annotations.ParameterObject;

import java.util.List;

@ParameterObject
public record RouteListRequest(
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
            description = "정렬 타입 (기본 값: 최근 생성 순)",
            example = "CREATED_AT_DESC",
            schema = @Schema(defaultValue = "CREATED_AT_DESC")
    )
    RouteSortType sortType,
    
    @Parameter(
            description = "관계 타입 필터 (기본 값: 전체 선택 / 다중 선택 가능)",
            example = "OWNER,SHARED"
    )
    List<RouteRelationType> relationTypes,
    
    @Parameter(
            description = "최소 거리 (km)",
            example = "0.0",
            required = true
    )
    Double minDistanceKm,
    
    @Parameter(
            description = "최대 거리 (km)",
            example = "50.0",
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
            example = "500.0",
            required = true
    )
    Double maxElevationGain
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