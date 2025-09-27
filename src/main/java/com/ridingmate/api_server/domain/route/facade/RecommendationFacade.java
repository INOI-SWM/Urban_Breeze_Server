package com.ridingmate.api_server.domain.route.facade;

import com.ggalmazor.ltdownsampling.Point;
import com.ridingmate.api_server.domain.route.dto.request.RecommendationListRequest;
import com.ridingmate.api_server.domain.route.dto.FilterRangeInfo;
import com.ridingmate.api_server.domain.route.dto.response.CreateRouteResponse;
import com.ridingmate.api_server.domain.route.dto.response.RecommendationDetailResponse;
import com.ridingmate.api_server.domain.route.dto.response.RecommendationListResponse;
import com.ridingmate.api_server.domain.route.entity.Recommendation;
import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.route.service.RouteService;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.service.UserService;
import com.ridingmate.api_server.global.dto.PaginationResponse;
import com.ridingmate.api_server.global.util.GeometryUtil;
import com.ridingmate.api_server.global.util.GpxGenerator;
import com.ridingmate.api_server.infra.aws.s3.S3Manager;
import com.ridingmate.api_server.infra.geoapify.GeoapifyClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * 추천 코스 관련 Facade
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationFacade {

    private final GeoapifyClient geoapifyClient;
    private final RouteService routeService;
    private final S3Manager s3Manager;
    private final UserService userService;

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

        return new RecommendationListResponse(recommendationItems, PaginationResponse.from(routePage));
    }

    public CreateRouteResponse copyRecommendedRouteToMyRoutes(Long userId, String routeId) {
        User user = userService.getUser(userId);
        Route route = routeService.copyRecommendedRoute(user,  routeId);

        // 썸네일 이미지 S3 업로드
        byte[] thumbnailBytes = geoapifyClient.getStaticMap(route.getRouteLine());
        String thumbnailImagePath = routeService.createThumbnailImagePath(route.getRouteId().toString());
        s3Manager.uploadByteFiles(thumbnailImagePath, thumbnailBytes, "image/png");
        routeService.updateThumbnailImagePath(route, thumbnailImagePath);

        // GPX 파일 생성 및 S3 업로드
        try {
            String gpxFilePath = GpxGenerator.generateGpxFilePath(route.getId());
            Coordinate[] coordinates = routeService.getRouteDetailList(route.getId());
            byte[] gpxBytes = GpxGenerator.generateGpxBytesFromCoordinates(coordinates, route.getTitle());
            s3Manager.uploadByteFiles(gpxFilePath, gpxBytes, "application/gpx+xml");
            routeService.updateGpxFilePath(route, gpxFilePath);
        } catch (IOException e) {
            throw new RuntimeException("GPX 파일 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }

        return CreateRouteResponse.from(route);
    }

    /**
     * 추천 코스 세부 정보 조회
     * @param routeId 추천 코스 ID
     * @return 추천 코스 세부 정보 응답
     */
    public RecommendationDetailResponse getRecommendationDetail(String routeId) {
        Route route = routeService.getRecommendationRouteWithUserByRouteId(routeId);
        Coordinate[] coordinates = routeService.getRouteDetailList(route.getId());

        List<Point> elevationProfilePoints = GeometryUtil.downsampleElevationProfile(coordinates, route.getDistance());

        String profileImageUrl = s3Manager.getPresignedUrl(route.getUser().getProfileImagePath());

        return RecommendationDetailResponse.from(route, elevationProfilePoints, profileImageUrl);
    }

} 