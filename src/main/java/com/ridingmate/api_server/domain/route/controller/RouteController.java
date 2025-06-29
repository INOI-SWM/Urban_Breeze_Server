package com.ridingmate.api_server.domain.route.controller;

import com.ridingmate.api_server.domain.route.dto.request.CreateRouteRequest;
import com.ridingmate.api_server.domain.route.dto.request.RouteSegmentRequest;
import com.ridingmate.api_server.domain.route.dto.response.CreateRouteResponse;
import com.ridingmate.api_server.domain.route.dto.response.RouteSegmentResponse;
import com.ridingmate.api_server.domain.route.exception.RouteSuccessCode;
import com.ridingmate.api_server.domain.route.facade.RouteFacade;
import com.ridingmate.api_server.global.exception.ApiResponse;
import com.ridingmate.api_server.global.exception.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteFacade routeFacade;

    @PostMapping("/segment")
    public ResponseEntity<ApiResponse<RouteSegmentResponse>>previewRoute(@RequestBody RouteSegmentRequest request) {
        RouteSegmentResponse response = routeFacade.generateSegment(request);
        return ResponseEntity
                .status(RouteSuccessCode.SEGMENT_CREATED.getStatus())
                .body(ApiResponse.success(RouteSuccessCode.SEGMENT_CREATED, response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateRouteResponse>> createRoute(@Valid @RequestBody CreateRouteRequest request) {
        CreateRouteResponse response = routeFacade.createRoute(request);
        return ResponseEntity.
                status(RouteSuccessCode.ROUTE_CREATED.getStatus())
                .body(ApiResponse.success(RouteSuccessCode.ROUTE_CREATED, response));
    }
}
