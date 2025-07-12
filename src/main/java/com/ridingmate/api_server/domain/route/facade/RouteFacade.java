package com.ridingmate.api_server.domain.route.facade;

import com.ridingmate.api_server.domain.route.dto.request.CreateRouteRequest;
import com.ridingmate.api_server.domain.route.dto.request.RouteSegmentRequest;
import com.ridingmate.api_server.domain.route.dto.response.CreateRouteResponse;
import com.ridingmate.api_server.domain.route.dto.response.RouteListItemResponse;
import com.ridingmate.api_server.domain.route.dto.response.RouteListResponse;
import com.ridingmate.api_server.domain.route.dto.response.RouteSegmentResponse;
import com.ridingmate.api_server.domain.route.dto.response.ShareRouteResponse;
import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.route.enums.RouteSortType;
import com.ridingmate.api_server.domain.route.service.RouteService;
import com.ridingmate.api_server.infra.aws.s3.S3Manager;
import com.ridingmate.api_server.infra.geoapify.GeoapifyClient;
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

        double distanceKm = new BigDecimal(route.getTotalDistance())
                .setScale(2, RoundingMode.DOWN)
                .doubleValue();

        return new CreateRouteResponse(
                route.getId(),
                route.getTitle(),
                route.getTotalDuration().toMinutes(),
                distanceKm,
                route.getTotalElevationGain()
                );
    }

    public ShareRouteResponse shareRoute(Long routeId) {
        String shareLink = routeService.createShareLink(routeId);
        return new ShareRouteResponse(shareLink);
    }

    /**
     * 정렬 타입과 함께 사용자별 경로 목록 조회
     * @param userId 사용자 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sortType 정렬 타입
     * @return 정렬된 경로 목록 응답
     */
    public RouteListResponse getRouteList(Long userId, int page, int size, RouteSortType sortType) {
        Page<Route> routePage = routeService.getRoutesByUser(userId, page, size, sortType);
        
        List<RouteListItemResponse> routeItems = routePage.getContent()
                .stream()
                .map(this::convertToRouteListItem)
                .toList();

        RouteListResponse.PaginationResponse pagination = new RouteListResponse.PaginationResponse(
                routePage.getNumber(),
                routePage.getTotalPages(),
                routePage.getTotalElements(),
                routePage.getSize(),
                routePage.hasNext(),
                routePage.hasPrevious()
        );

        return new RouteListResponse(routeItems, pagination);
    }

    private RouteListItemResponse convertToRouteListItem(Route route) {
        // 거리를 km 단위로 변환 (소수점 2자리)
        double distanceKm = new BigDecimal(route.getTotalDistance() / 1000.0)
                .setScale(2, RoundingMode.DOWN)
                .doubleValue();

        // S3 URL 생성
        String thumbnailUrl = s3Manager.getPresignedUrl(route.getThumbnailImagePath());

        return new RouteListItemResponse(
                route.getId(),
                route.getTitle(),
                thumbnailUrl,
                route.getCreatedAt(),
                distanceKm,
                route.getTotalElevationGain()
        );
    }
}
