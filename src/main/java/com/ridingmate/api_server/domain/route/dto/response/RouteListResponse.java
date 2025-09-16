package com.ridingmate.api_server.domain.route.dto.response;

import com.ridingmate.api_server.domain.route.dto.FilterRangeInfo;
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
     * RouteListItemResponse 리스트, Route 페이지, 전체 데이터 기준 FilterRangeInfo로부터 RouteListResponse 생성
     * @param routeItems 변환된 경로 목록
     * @param routePage Route 페이지 (페이지네이션 정보 추출용)
     * @param filterRange 전체 데이터 기준 필터링 범위 정보
     * @return RouteListResponse DTO
     */
    public static RouteListResponse of(List<RouteListItemResponse> routeItems, Page<Route> routePage, FilterRangeInfo filterRange) {
        PaginationResponse pagination = PaginationResponse.from(routePage);
        return new RouteListResponse(routeItems, pagination, filterRange);
    }
}