package com.ridingmate.api_server.domain.route.controller;

import com.ridingmate.api_server.domain.route.dto.request.CreateRouteRequest;
import com.ridingmate.api_server.domain.route.dto.request.RouteSegmentRequest;
import com.ridingmate.api_server.domain.route.dto.response.CreateRouteResponse;
import com.ridingmate.api_server.domain.route.dto.response.RouteSegmentResponse;
import com.ridingmate.api_server.domain.route.dto.response.ShareRouteResponse;
import com.ridingmate.api_server.global.exception.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Route API", description = "경로 기능 API")
public interface RouteApi {

    @Operation(summary = "경로 생성을 위한 핀 단위 경로 생성", description = "시작점과 도착점을 통해 자전거 경로를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "성공: 세그먼트 생성 완료"),
    })
    ResponseEntity<CommonResponse<RouteSegmentResponse>> previewRoute(@RequestBody RouteSegmentRequest request);

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
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "성공: 경로 생성 완료"),
    })
    ResponseEntity<CommonResponse<CreateRouteResponse>> createRoute(@Valid @RequestBody CreateRouteRequest request);

    @Operation(
            summary = "경로 공유 링크 조회",
            description = """
            특정 경로(Route)에 대해 미리 생성된 공유 ID를 기반으로
            딥링크 형식의 공유 URL을 반환합니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공: 공유 링크 조회 완료"),
    })
    ResponseEntity<CommonResponse<ShareRouteResponse>> shareRoute(@PathVariable Long routeId);
}