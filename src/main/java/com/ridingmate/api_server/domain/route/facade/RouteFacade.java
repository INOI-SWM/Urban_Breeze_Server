package com.ridingmate.api_server.domain.route.facade;

import com.ridingmate.api_server.domain.route.dto.request.CreateRouteRequest;
import com.ridingmate.api_server.domain.route.dto.request.RouteSegmentRequest;
import com.ridingmate.api_server.domain.route.dto.request.RouteListRequest;
import com.ridingmate.api_server.domain.route.dto.response.*;
import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.route.service.RouteService;
import com.ridingmate.api_server.infra.kakao.KakaoMapper;
import com.ridingmate.api_server.infra.aws.s3.S3Manager;
import com.ridingmate.api_server.infra.geoapify.GeoapifyClient;
import com.ridingmate.api_server.infra.kakao.KakaoClient;
import com.ridingmate.api_server.infra.kakao.dto.request.KakaoSearchRequest;
import com.ridingmate.api_server.infra.kakao.dto.response.KakaoSearchResponse;
import com.ridingmate.api_server.infra.ors.OrsClient;
import com.ridingmate.api_server.infra.ors.OrsMapper;
import com.ridingmate.api_server.infra.ors.dto.response.OrsRouteResponse;
import com.ridingmate.api_server.global.util.GeometryUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.LineString;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RouteFacade {

    private final OrsClient orsClient;
    private final GeoapifyClient geoapifyClient;
    private final KakaoClient kakaoClient;
    private final KakaoMapper kakaoMapper;

    private final RouteService routeService;
    private final S3Manager s3Manager;

    public RouteSegmentResponse generateSegment(RouteSegmentRequest request) {
        OrsRouteResponse orsResponse = orsClient.getRoutePreview(request.toOrsRequest());
        return OrsMapper.toRouteSegmentResponse(orsResponse);
    }

    public CreateRouteResponse createRoute(CreateRouteRequest request) {
        LineString routeLine = GeometryUtil.polylineToLineString(request.polyline());
        byte[] thumbnailBytes = geoapifyClient.getStaticMap(routeLine);
        Route route = routeService.createRoute(request, routeLine);
        s3Manager.uploadByteFiles(route.getThumbnailImagePath(), thumbnailBytes);

        return CreateRouteResponse.from(route);
    }

    public ShareRouteResponse shareRoute(Long routeId) {
        String shareLink = routeService.createShareLink(routeId);
        return new ShareRouteResponse(shareLink);
    }

    /**
     * 정렬 타입과 필터와 함께 사용자별 경로 목록 조회
     * @param userId 사용자 ID
     * @param request 경로 목록 조회 요청 정보
     * @return 정렬된 경로 목록 응답
     */
    public RouteListResponse getRouteList(Long userId, RouteListRequest request) {
        // Service에서 경로 목록 조회
        Page<Route> routePage = routeService.getRoutesByUser(userId, request);

        // DTO 생성 시 썸네일 URL 추가
        List<RouteListItemResponse> routeItems = routePage.getContent().stream()
            .map(route -> {
                String thumbnailUrl = s3Manager.getPresignedUrl(route.getThumbnailImagePath());
                return RouteListItemResponse.from(route, thumbnailUrl);
            })
            .toList();

        return RouteListResponse.of(routeItems, routePage);
    }

    public MapSearchResponse getMapSearch(String query, Double lon, Double lat) {
        KakaoSearchResponse response = kakaoClient.searchPlaces(KakaoSearchRequest.from(query, lon, lat));
        return kakaoMapper.toMapSearchResponse(response);
    }
}
