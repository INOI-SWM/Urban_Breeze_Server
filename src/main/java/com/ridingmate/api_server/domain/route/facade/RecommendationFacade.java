package com.ridingmate.api_server.domain.route.facade;

import com.ridingmate.api_server.domain.route.dto.request.RecommendationListRequest;
import com.ridingmate.api_server.domain.route.dto.FilterRangeInfo;
import com.ridingmate.api_server.domain.route.dto.response.RecommendationListResponse;
import com.ridingmate.api_server.domain.route.entity.Recommendation;
import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.route.service.RouteService;
import com.ridingmate.api_server.global.dto.PaginationResponse;
import com.ridingmate.api_server.infra.aws.s3.S3Manager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 추천 코스 관련 Facade
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationFacade {

    private final RouteService routeService;
    private final S3Manager s3Manager;

    /**
     * 추천 코스 목록 조회
     * @param request 추천 코스 목록 조회 요청 정보
     * @return 추천 코스 목록 응답
     */
    public RecommendationListResponse getRecommendationList(RecommendationListRequest request) {
        // Service에서 추천 코스 목록 조회
        Page<Route> routePage = routeService.getRecommendationRoutes(request);

        // DTO 생성 시 썸네일 URL 추가
        List<RecommendationListResponse.RecommendationItemResponse> recommendationItems = routePage.getContent().stream()
            .map(route -> {
                String thumbnailUrl = s3Manager.getPresignedUrl(route.getThumbnailImagePath());
                // 추천 정보 조회
                Recommendation recommendation = route.getRecommendation();
                return RecommendationListResponse.RecommendationItemResponse.from(route, recommendation, thumbnailUrl);
            })
            .toList();

        // 전체 추천 코스 기준의 최대값 조회 (페이지 데이터가 아닌 전체 데이터 기준)
        FilterRangeInfo filterRangeInfo = routeService.getMaxDistanceAndElevationForRecommendations();

        return new RecommendationListResponse(recommendationItems, PaginationResponse.from(routePage), filterRangeInfo);
    }


} 