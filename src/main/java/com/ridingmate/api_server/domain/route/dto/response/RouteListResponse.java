package com.ridingmate.api_server.domain.route.dto.response;

import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.global.dto.PaginationResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;


public record RouteListResponse(
        @Schema(description = "경로 목록")
        List<RouteListItemResponse> routes,

        @Schema(description = "페이지네이션 정보")
        PaginationResponse pagination,

        @Schema(description = "필터링 범위 정보")
        FilterRangeInfo filterRange
) {

    /**
     * RouteListItemResponse 리스트와 Route 페이지로부터 RouteListResponse 생성
     * @param routeItems 변환된 경로 목록
     * @param routePage Route 페이지 (페이지네이션 정보 추출용)
     * @return RouteListResponse DTO
     */
    public static RouteListResponse of(List<RouteListItemResponse> routeItems, Page<Route> routePage) {
        PaginationResponse pagination = PaginationResponse.from(routePage);
        FilterRangeInfo filterRange = FilterRangeInfo.from(routeItems);
        return new RouteListResponse(routeItems, pagination, filterRange);
    }
}

/**
 * 필터링 범위 설정을 위한 최대값 정보
 */
@Schema(description = "필터링 범위 정보")
record FilterRangeInfo(
        @Schema(description = "최대 거리 (km)", example = "50.0")
        Double maxDistance,

        @Schema(description = "최대 상승고도 (m)", example = "1200.0")
        Double maxElevationGain
) {
    /**
     * RouteListItemResponse 리스트로부터 최대값들을 계산하여 FilterRangeInfo 생성
     */
    public static FilterRangeInfo from(List<RouteListItemResponse> routes) {
        if (routes.isEmpty()) {
            return new FilterRangeInfo(0.0, 0.0);
        }

        Double maxDistance = routes.stream()
                .mapToDouble(route -> route.distance() != null ? route.distance() : 0.0)
                .max()
                .orElse(0.0);

        Double maxElevationGain = routes.stream()
                .mapToDouble(route -> route.elevationGain() != null ? route.elevationGain() : 0.0)
                .max()
                .orElse(0.0);

        return new FilterRangeInfo(maxDistance, maxElevationGain);
    }
}