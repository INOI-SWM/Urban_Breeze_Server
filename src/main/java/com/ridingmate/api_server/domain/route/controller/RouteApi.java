package com.ridingmate.api_server.domain.route.controller;

import com.ridingmate.api_server.domain.route.dto.request.CreateRouteRequest;
import com.ridingmate.api_server.domain.route.dto.request.RouteSegmentRequest;
import com.ridingmate.api_server.domain.route.dto.response.CreateRouteResponse;
import com.ridingmate.api_server.domain.route.dto.response.RouteSegmentResponse;
import com.ridingmate.api_server.global.exception.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Route API", description = "경로 기능 API")
public interface RouteApi {

    @Operation(summary = "경로 생성을 위한 핀 단위 경로 생성", description = "시작점과 도착점을 통해 자전거 경로를 생성합니다.")
    public ResponseEntity<ApiResponse<RouteSegmentResponse>>previewRoute(@RequestBody RouteSegmentRequest request);

    @Operation(
            summary = "경로 생성 및 저장",
            description = """
        Polyline 정보를 기반으로 새로운 경로를 생성하고 저장합니다.
        
        - name: 경로 이름
        - polyline: Google Polyline 인코딩 형식
        - distance: 총 거리 (단위: 미터)
        - duration: 예상 소요 시간 (단위: 분)
        - elevationGain: 총 상승 고도 (단위: 미터)
        """
    )
    public ResponseEntity<ApiResponse<CreateRouteResponse>> createRoute(@Valid @RequestBody CreateRouteRequest request);
}
