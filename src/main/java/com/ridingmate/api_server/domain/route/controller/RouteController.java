package com.ridingmate.api_server.domain.route.controller;

import com.ridingmate.api_server.domain.auth.exception.AuthErrorCode;
import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.domain.route.dto.request.AddRouteToMyRoutesRequest;
import com.ridingmate.api_server.domain.route.dto.request.CopyRecommendedRouteRequest;
import com.ridingmate.api_server.domain.route.dto.request.CreateRouteRequest;
import com.ridingmate.api_server.domain.route.dto.request.RouteListRequest;
import com.ridingmate.api_server.domain.route.dto.request.RouteSegmentRequest;
import com.ridingmate.api_server.domain.route.dto.response.*;
import com.ridingmate.api_server.domain.route.exception.code.RouteCommonErrorCode;
import com.ridingmate.api_server.domain.route.exception.RouteSuccessCode;
import com.ridingmate.api_server.domain.route.exception.code.RouteCreationErrorCode;
import com.ridingmate.api_server.domain.route.facade.RouteFacade;
import com.ridingmate.api_server.global.exception.ApiErrorCodeExample;
import com.ridingmate.api_server.global.exception.CommonResponse;
import com.ridingmate.api_server.infra.kakao.KakaoErrorCode;
import com.ridingmate.api_server.infra.ors.OrsErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController implements RouteApi{

    private final RouteFacade routeFacade;

    @Override
    @PostMapping("/segment")
    @ApiErrorCodeExample(RouteCommonErrorCode.class)
    @ApiErrorCodeExample(OrsErrorCode.class)
    public ResponseEntity<CommonResponse<RouteSegmentResponse>>previewRoute(@RequestBody RouteSegmentRequest request) {
        RouteSegmentResponse response = routeFacade.generateSegment(request);
        return ResponseEntity
                .status(RouteSuccessCode.SEGMENT_CREATED.getStatus())
                .body(CommonResponse.success(RouteSuccessCode.SEGMENT_CREATED, response));
    }

    @Override
    @PostMapping
    @ApiErrorCodeExample(RouteCreationErrorCode.class)
    @ApiErrorCodeExample(AuthErrorCode.class)
    public ResponseEntity<CommonResponse<CreateRouteResponse>> createRoute(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CreateRouteRequest request) {
        CreateRouteResponse response = routeFacade.createRoute(authUser, request);
        return ResponseEntity.
                status(RouteSuccessCode.ROUTE_CREATED.getStatus())
                .body(CommonResponse.success(RouteSuccessCode.ROUTE_CREATED, response));
    }

    @Override
    @GetMapping("/{routeId}/share")
    @ApiErrorCodeExample(RouteCommonErrorCode.class)
    public ResponseEntity<CommonResponse<ShareRouteResponse>> shareRoute(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String routeId) {
        ShareRouteResponse  response = routeFacade.shareRoute(authUser, routeId);
        return ResponseEntity
                .status(RouteSuccessCode.SHARE_LINK_FETCHED.getStatus())
                .body(CommonResponse.success(RouteSuccessCode.SHARE_LINK_FETCHED, response));
    }

    @Override
    @GetMapping
    public ResponseEntity<CommonResponse<RouteListResponse>> getRouteList(
            @AuthenticationPrincipal AuthUser authUser,
            @ModelAttribute RouteListRequest request
    ) {
        RouteListResponse response = routeFacade.getRouteList(authUser, request);
        return ResponseEntity
                .status(RouteSuccessCode.ROUTE_LIST_FETCHED.getStatus())
                .body(CommonResponse.success(RouteSuccessCode.ROUTE_LIST_FETCHED, response));
    }

    @Override
    @GetMapping("/{routeId}")
    @ApiErrorCodeExample(RouteCommonErrorCode.class)
    public ResponseEntity<CommonResponse<RouteDetailResponse>> getRouteDetail(
        @PathVariable String routeId
    ) {
        RouteDetailResponse response = routeFacade.getRouteDetail(routeId);
        return ResponseEntity
            .status(RouteSuccessCode.ROUTE_DETAIL_FETCHED.getStatus())
            .body(CommonResponse.success(RouteSuccessCode.ROUTE_DETAIL_FETCHED, response));
    }

    @Override
    @GetMapping("/search")
    @ApiErrorCodeExample(KakaoErrorCode.class)
    public ResponseEntity<CommonResponse<MapSearchResponse>> getMapSearch(
            @RequestParam String query,
            @RequestParam Double lon,
            @RequestParam Double lat) {
        MapSearchResponse response = routeFacade.getMapSearch(query, lon, lat);
        return ResponseEntity
                .status(RouteSuccessCode.MAP_SEARCH_FETCHED.getStatus())
                .body(CommonResponse.success(RouteSuccessCode.MAP_SEARCH_FETCHED, response));
    }

    @Override
    @GetMapping("/{routeId}/gpx")
    @ApiErrorCodeExample(RouteCommonErrorCode.class)
    public ResponseEntity<byte[]> downloadGpxFile(@PathVariable String routeId) {
        GpxDownloadInfo downloadInfo = routeFacade.downloadGpxFile(routeId);

        String encodedFileName = URLEncoder.encode(downloadInfo.fileName(), StandardCharsets.UTF_8);
        
        return ResponseEntity.ok()
                .header("Content-Type", downloadInfo.contentType())
                .header("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"")
                .body(downloadInfo.content());
    }

    @Override
    @PostMapping("/my-routes")
    @ApiErrorCodeExample(RouteCommonErrorCode.class)
    @ApiErrorCodeExample(AuthErrorCode.class)
    public ResponseEntity<CommonResponse<Void>> addRouteToMyRoutes(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody AddRouteToMyRoutesRequest request) {
        
        routeFacade.addRouteToMyRoutes(authUser.id(), request);
        
        return ResponseEntity
                .status(RouteSuccessCode.ROUTE_ADDED_TO_MY_ROUTES.getStatus())
                .body(CommonResponse.success(RouteSuccessCode.ROUTE_ADDED_TO_MY_ROUTES, null));
    }

    @Override
    @PostMapping("/copy")
    @ApiErrorCodeExample(RouteCommonErrorCode.class)
    @ApiErrorCodeExample(AuthErrorCode.class)
    public ResponseEntity<CommonResponse<CreateRouteResponse>> copyRecommendedRoute(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CopyRecommendedRouteRequest request) {

        CreateRouteResponse response = routeFacade.copyRecommendedRoute(authUser.id(), request);
        
        return ResponseEntity
                .status(RouteSuccessCode.ROUTE_COPIED.getStatus())
                .body(CommonResponse.success(RouteSuccessCode.ROUTE_COPIED, response));
    }
}
