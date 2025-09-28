package com.ridingmate.api_server.domain.route.facade;

import com.ggalmazor.ltdownsampling.Point;
import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.domain.route.dto.FilterRangeInfo;
import com.ridingmate.api_server.domain.route.dto.request.AddRouteToMyRoutesRequest;
import com.ridingmate.api_server.domain.route.dto.request.CreateRouteRequest;
import com.ridingmate.api_server.domain.route.dto.request.RouteSegmentRequest;
import com.ridingmate.api_server.domain.route.dto.request.RouteListRequest;
import com.ridingmate.api_server.domain.route.dto.response.*;
import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.route.service.RouteService;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.service.UserService;
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
import com.ridingmate.api_server.global.util.GpxGenerator;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RouteFacade {

    private final OrsClient orsClient;
    private final GeoapifyClient geoapifyClient;
    private final KakaoClient kakaoClient;
    private final KakaoMapper kakaoMapper;

    private final RouteService routeService;
    private final S3Manager s3Manager;
    private final UserService userService;

    public RouteSegmentResponse generateSegment(RouteSegmentRequest request) {
        OrsRouteResponse orsResponse = orsClient.getRoutePreview(request.toOrsRequest());
        return OrsMapper.toRouteSegmentResponse(orsResponse);
    }

    public CreateRouteResponse createRoute(AuthUser authUser, CreateRouteRequest request) {
        LineString routeLine = GeometryUtil.polylineToLineString(request.polyline());
        Route route = routeService.createRoute(authUser.id(), request, routeLine);

        // 썸네일 이미지 S3 업로드
        byte[] thumbnailBytes = geoapifyClient.getStaticMap(routeLine);
        String thumbnailImagePath =  routeService.createThumbnailImagePath(route.getRouteId().toString());
        s3Manager.uploadByteFiles(thumbnailImagePath ,thumbnailBytes, "image/png");
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

    public ShareRouteResponse shareRoute(AuthUser authUser, String routeId) {
        Route route = routeService.getRouteWithUserByRouteId(routeId);
        String shareLink = routeService.createShareLink(route, authUser.id());
        return new ShareRouteResponse(shareLink);
    }

    /**
     * 정렬 타입과 필터와 함께 사용자별 경로 목록 조회
     * @param authUser 사용자 ID
     * @param request 경로 목록 조회 요청 정보
     * @return 정렬된 경로 목록 응답
     */
    public RouteListResponse getRouteList(AuthUser authUser, RouteListRequest request) {
        // Service에서 경로 목록 조회
        Page<Route> routePage = routeService.getRoutesByUser(authUser.id(), request);
        User user = userService.getUser(authUser.id());

        // DTO 생성 시 썸네일 URL과 프로필 이미지 URL 추가
        List<RouteListItemResponse> routeItems = routePage.getContent().stream()
            .map(route -> {
                String thumbnailUrl = s3Manager.getPresignedUrl(route.getThumbnailImagePath());
                String profileImageUrl = s3Manager.getPresignedUrl(route.getUser().getProfileImagePath());
                return RouteListItemResponse.from(route, thumbnailUrl, profileImageUrl);
            })
            .toList();

        // 전체 데이터 기준의 최대값 조회 (페이지 데이터가 아닌 전체 데이터 기준)
        FilterRangeInfo filterRangeInfo = routeService.getMaxDistanceAndElevationByUser(user);

        return RouteListResponse.of(routeItems, routePage, filterRangeInfo);
    }

    public RouteDetailResponse getRouteDetail(String routeId){
        Route route = routeService.getRouteWithUserByRouteId(routeId);
        Coordinate[] coordinates = routeService.getRouteDetailList(route.getId());

        // 고도 프로필 다운샘플링 (GeometryUtil에서 모든 로직 처리)
        List<Point> elevationProfilePoints = GeometryUtil.downsampleElevationProfile(coordinates, route.getDistance());

        String profileImageUrl = s3Manager.getPresignedUrl(route.getUser().getProfileImagePath());

        return RouteDetailResponse.from(route, elevationProfilePoints, profileImageUrl);
    }

    public MapSearchResponse getMapSearch(String query, Double lon, Double lat) {
        KakaoSearchResponse response = kakaoClient.searchPlaces(KakaoSearchRequest.from(query, lon, lat));
        return kakaoMapper.toMapSearchResponse(response);
    }

    public GpxDownloadInfo downloadGpxFile(String routeId) {
        Route route = routeService.getRouteWithUserByRouteId(routeId);  // 1번만 조회
        byte[] content = routeService.downloadGpxFile(route);
        String fileName = routeService.generateGpxFileName(route);
        
        return GpxDownloadInfo.of(content, fileName);
    }

    /**
     * 내 경로에 추가
     */
    public void addRouteToMyRoutes(Long userId, AddRouteToMyRoutesRequest request) {
        User user = userService.getUser(userId);
        routeService.addRouteToMyRoutes(user, request);
    }

    /**
     * 경로 삭제
     */
    public void deleteRoute(Long userId, String routeId) {
        User user = userService.getUser(userId);
        Route route = routeService.getRouteByRouteId(routeId);
        routeService.deleteRoute(user, route);
    }

}
