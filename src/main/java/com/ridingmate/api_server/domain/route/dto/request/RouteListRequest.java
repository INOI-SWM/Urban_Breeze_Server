package com.ridingmate.api_server.domain.route.dto.request;

import com.ridingmate.api_server.domain.route.enums.RouteRelationType;
import com.ridingmate.api_server.domain.route.enums.RouteSortType;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RouteListRequest {
    
    @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
    private int page = 0;
    
    @Parameter(description = "페이지 크기", example = "3")
    private int size = 3;
    
    @Parameter(description = "정렬 타입", example = "CREATED_AT_DESC")
    private RouteSortType sortType = RouteSortType.CREATED_AT_DESC;
    
    @Parameter(description = "관계 타입 필터 (여러 값 지정 가능)", example = "OWNER,SHARED")
    private List<RouteRelationType> relationTypes;
    
    @Parameter(description = "최소 거리 (km)", example = "10.0")
    private Double minDistanceKm;
    
    @Parameter(description = "최대 거리 (km)", example = "50.0")
    private Double maxDistanceKm;
    
    @Parameter(description = "최소 고도 상승 (미터)", example = "100.0")
    private Double minElevationGain;
    
    @Parameter(description = "최대 고도 상승 (미터)", example = "500.0")
    private Double maxElevationGain;
    
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