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
        PaginationResponse pagination
) {

    /**
     * RouteListItemResponse 리스트와 Route 페이지로부터 RouteListResponse 생성
     * @param routeItems 변환된 경로 목록
     * @param routePage Route 페이지 (페이지네이션 정보 추출용)
     * @return RouteListResponse DTO
     */
    public static RouteListResponse of(List<RouteListItemResponse> routeItems, Page<Route> routePage) {
        PaginationResponse pagination = PaginationResponse.from(routePage);
        return new RouteListResponse(routeItems, pagination);
    }
} 