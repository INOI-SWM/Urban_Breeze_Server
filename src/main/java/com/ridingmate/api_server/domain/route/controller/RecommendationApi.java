package com.ridingmate.api_server.domain.route.controller;

import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.domain.route.dto.request.RecommendationListRequest;
import com.ridingmate.api_server.domain.route.dto.response.CreateRouteResponse;
import com.ridingmate.api_server.domain.route.dto.response.RecommendationListResponse;
import com.ridingmate.api_server.global.exception.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Recommendation API", description = "추천 코스 기능 API")
public interface RecommendationApi {

    @Operation(
            summary = "추천 코스 목록 조회",
            description = """
            추천 코스 목록을 페이지네이션과 정렬 옵션, 필터로 조회합니다.
            
            - 페이지 크기: 10개 (기본값)
            - 정렬 옵션:
              * NEAREST: 가까운 순 (기본값, 사용자 위치 필요)
              * DISTANCE_LONG: 거리 긴 순
              * DISTANCE_SHORT: 거리 짧은 순
              * DIFFICULTY_HIGH: 난이도 높은 순
              * DIFFICULTY_LOW: 난이도 낮은 순
            - 필터 옵션:
              * 추천 타입: 국토 종주, 대회 코스, 유명 코스
              * 지역: 서울, 경기, 강원 등
              * 난이도: 쉬움, 보통, 어려움
              * 거리 필터: 최소/최대 거리 범위 지정 (km 단위)
              * 고도 필터: 최소/최대 고도 상승 범위 지정 (미터 단위)
            - 포함 정보: 제목, 썸네일, 거리, 상승고도, 난이도, 추천 타입
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공: 추천 코스 목록 조회 완료"),
    })
     ResponseEntity<CommonResponse<RecommendationListResponse>> getRecommendationList(@ModelAttribute RecommendationListRequest request);

     @Operation(
             summary = "추천 코스 나의 경로에 저장",
             description = "추천받은 코스를 내 경로로 복사하여 저장합니다. 새로운 경로가 생성되며 모든 GPS 로그가 복사됩니다."
     )
     @ApiResponses({
             @ApiResponse(responseCode = "201", description = "성공: 추천 코스가 내 경로로 저장되었습니다."),
             @ApiResponse(responseCode = "404", description = "경로를 찾을 수 없습니다."),
     })
     ResponseEntity<CommonResponse<CreateRouteResponse>> copyRecommendedRouteToMyRoutes(
             @AuthenticationPrincipal AuthUser authUser,
             @Parameter(description = "복사할 경로 ID")
             @PathVariable String routeId
     );
}