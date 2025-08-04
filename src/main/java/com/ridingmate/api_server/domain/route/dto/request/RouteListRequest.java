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
            description = "페이지 번호 (0부터 시작)",
            example = "0",
            schema = @Schema(defaultValue = "0"),
            required = true
    )
    int page,
    
    @Parameter(
            description = "페이지 크기",
            example = "3",
            schema = @Schema(defaultValue = "3"),
            required = true
    )
    int size,
    
    @Parameter(
            description = "정렬 타입",
            example = "CREATED_AT_DESC",
            schema = @Schema(defaultValue = "CREATED_AT_DESC"),
            required = true
    )
    RouteSortType sortType,
    
    @Parameter(
            description = "관계 타입 필터 (여러 값 지정 가능, 미지정시 전체 선택)",
            example = "OWNER,SHARED"
    )
    List<RouteRelationType> relationTypes,
    
    @Parameter(
            description = "최소 거리 (km)",
            example = "10.0",
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
            example = "100.0",
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