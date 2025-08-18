package com.ridingmate.api_server.domain.route.controller;

import com.ridingmate.api_server.domain.route.dto.request.RecommendationListRequest;
import com.ridingmate.api_server.domain.route.dto.response.RecommendationListResponse;
import com.ridingmate.api_server.domain.route.exception.RouteSuccessCode;
import com.ridingmate.api_server.domain.route.facade.RecommendationFacade;
import com.ridingmate.api_server.global.exception.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
} 