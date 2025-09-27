package com.ridingmate.api_server.domain.route.controller;

import com.ridingmate.api_server.domain.auth.exception.AuthErrorCode;
import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.domain.route.dto.request.RecommendationListRequest;
import com.ridingmate.api_server.domain.route.dto.response.CreateRouteResponse;
import com.ridingmate.api_server.domain.route.dto.response.RecommendationListResponse;
import com.ridingmate.api_server.domain.route.exception.RouteSuccessCode;
import com.ridingmate.api_server.domain.route.exception.code.RouteCommonErrorCode;
import com.ridingmate.api_server.domain.route.facade.RecommendationFacade;
import com.ridingmate.api_server.global.exception.ApiErrorCodeExample;
import com.ridingmate.api_server.global.exception.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController implements RecommendationApi {

    private final RecommendationFacade recommendationFacade;

    @Override
    @GetMapping
    public ResponseEntity<CommonResponse<RecommendationListResponse>> getRecommendationList(
            @ModelAttribute RecommendationListRequest request
    ) {
        RecommendationListResponse response = recommendationFacade.getRecommendationList(request);
        return ResponseEntity
                .status(RouteSuccessCode.ROUTE_LIST_FETCHED.getStatus())
                .body(CommonResponse.success(RouteSuccessCode.ROUTE_LIST_FETCHED, response));
    }

    @Override
    @PostMapping("/routes/{routeId}")
    @ApiErrorCodeExample(RouteCommonErrorCode.class)
    @ApiErrorCodeExample(AuthErrorCode.class)
    public ResponseEntity<CommonResponse<CreateRouteResponse>> copyRecommendedRouteToMyRoutes(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String routeId) {

        CreateRouteResponse response = recommendationFacade.copyRecommendedRouteToMyRoutes(authUser.id(), routeId);

        return ResponseEntity
                .status(RouteSuccessCode.RECOMMENDED_ROUTE_COPIED.getStatus())
                .body(CommonResponse.success(RouteSuccessCode.RECOMMENDED_ROUTE_COPIED, response));
    }
} 