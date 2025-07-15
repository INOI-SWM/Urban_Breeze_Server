package com.ridingmate.api_server.domain.route.controller;

import com.ridingmate.api_server.domain.route.dto.request.CreateRouteRequest;
import com.ridingmate.api_server.domain.route.dto.request.RouteSegmentRequest;
import com.ridingmate.api_server.domain.route.dto.response.CreateRouteResponse;
import com.ridingmate.api_server.domain.route.dto.response.RouteListResponse;
import com.ridingmate.api_server.domain.route.dto.response.RouteSegmentResponse;
import com.ridingmate.api_server.domain.route.dto.response.ShareRouteResponse;
import com.ridingmate.api_server.domain.route.enums.RouteRelationType;
import com.ridingmate.api_server.domain.route.enums.RouteSortType;
import com.ridingmate.api_server.domain.route.exception.RouteSuccessCode;
import com.ridingmate.api_server.domain.route.facade.RouteFacade;
import com.ridingmate.api_server.global.exception.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController implements RouteApi{

    private final RouteFacade routeFacade;

    @Override
    @PostMapping("/segment")
    public ResponseEntity<CommonResponse<RouteSegmentResponse>>previewRoute(@RequestBody RouteSegmentRequest request) {
        RouteSegmentResponse response = routeFacade.generateSegment(request);
        return ResponseEntity
                .status(RouteSuccessCode.SEGMENT_CREATED.getStatus())
                .body(CommonResponse.success(RouteSuccessCode.SEGMENT_CREATED, response));
    }

    @Override
    @PostMapping
    public ResponseEntity<CommonResponse<CreateRouteResponse>> createRoute(@Valid @RequestBody CreateRouteRequest request) {
        CreateRouteResponse response = routeFacade.createRoute(request);
        return ResponseEntity.
                status(RouteSuccessCode.ROUTE_CREATED.getStatus())
                .body(CommonResponse.success(RouteSuccessCode.ROUTE_CREATED, response));
    }

    @Override
    @GetMapping("/{routeId}/share")
    public ResponseEntity<CommonResponse<ShareRouteResponse>> shareRoute(@PathVariable Long routeId) {
        ShareRouteResponse  response = routeFacade.shareRoute(routeId);
        return ResponseEntity
                .status(RouteSuccessCode.SHARE_LINK_FETCHED.getStatus())
                .body(CommonResponse.success(RouteSuccessCode.SHARE_LINK_FETCHED, response));
    }

    @Override
    @GetMapping
    public ResponseEntity<CommonResponse<RouteListResponse>> getRouteList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "CREATED_AT_DESC") RouteSortType sortBy,
            @RequestParam(required = false) List<RouteRelationType> filter) {
        
        // 페이지 크기 3개 고정
        int pageSize = 3;
        
        // TODO: 실제 사용자 인증 구현 후 수정 필요
        Long userId = 1L; // 현재는 mockUser 사용
        
        RouteListResponse response = routeFacade.getRouteList(userId, page, pageSize, sortBy, filter);
        return ResponseEntity
                .status(RouteSuccessCode.ROUTE_LIST_FETCHED.getStatus())
                .body(CommonResponse.success(RouteSuccessCode.ROUTE_LIST_FETCHED, response));
    }
}
