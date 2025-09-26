package com.ridingmate.api_server.domain.route.controller;

import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.domain.route.dto.request.AddRouteToMyRoutesRequest;
import com.ridingmate.api_server.domain.route.dto.request.CopyRecommendedRouteRequest;
import com.ridingmate.api_server.domain.route.dto.request.CreateRouteRequest;
import com.ridingmate.api_server.domain.route.dto.request.RouteListRequest;
import com.ridingmate.api_server.domain.route.dto.request.RouteSegmentRequest;
import com.ridingmate.api_server.domain.route.dto.response.*;
import com.ridingmate.api_server.global.exception.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Route API", description = "경로 기능 API")
public interface RouteApi {

    @Operation(summary = "경로 생성을 위한 핀 단위 경로 생성", description = "시작점과 도착점을 통해 자전거 경로를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "성공: 세그먼트 생성 완료"),
    })
    ResponseEntity<CommonResponse<RouteSegmentResponse>> previewRoute(
            @RequestBody RouteSegmentRequest request);

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
    ResponseEntity<CommonResponse<CreateRouteResponse>> createRoute(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CreateRouteRequest request);

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
    ResponseEntity<CommonResponse<ShareRouteResponse>> shareRoute(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String routeId);

    @Operation(
            summary = "내 경로 목록 조회",
            description = """
            현재 사용자가 생성한 경로 목록을 페이지네이션과 정렬 옵션, 필터로 조회합니다.
            
            - 페이지 크기: 3개 (기본값)
            - 정렬 옵션:
              * CREATED_AT_DESC: 최신순 (기본값)
              * CREATED_AT_ASC: 오래된순
              * DISTANCE_ASC: 주행거리 오름차순
              * DISTANCE_DESC: 주행거리 내림차순
            - 필터 옵션:
              * OWNER: 사용자가 직접 생성한 경로
              * SHARED: 공유받은 경로
              * 여러 값 지정 가능 (쉼표로 구분)
            - 거리 필터: 최소/최대 거리 범위 지정 (km 단위)
            - 고도 필터: 최소/최대 고도 상승 범위 지정 (미터 단위)
            - 포함 정보: 제목, 썸네일, 생성일, 이동거리, 상승고도
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공: 경로 목록 조회 완료"),
    })
    ResponseEntity<CommonResponse<RouteListResponse>> getRouteList(
            @AuthenticationPrincipal AuthUser authUser,
            @ModelAttribute RouteListRequest request);

    @Operation(
            summary = "경로 상세 정보 조회",
            description = """
                    특정 경로(Route)의 상세 정보를 조회합니다.
                                        
                    - 경로의 기본 정보(이름, 설명, 거리, 고도 등)
                    - 경로를 구성하는 모든 GPS 좌표 목록 (위도, 경도, 고도)
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공: 경로 상세 정보 조회 완료"),
            @ApiResponse(responseCode = "403", description = "접근 권한이 없는 경로입니다."),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 경로입니다."),
    })
    ResponseEntity<CommonResponse<RouteDetailResponse>> getRouteDetail(
            @Parameter(description = "경로 ID")
            @PathVariable String routeId);

    @Operation(
            summary = "지도 장소 검색",
            description = """
                    카카오 API를 통해 특정 위치 주변의 장소를 검색합니다.
                    
                    검색 키워드에 장소와 관련된 키워드가 있다면 장소 기준으로 검색을 합니다.
                    만약 검색키워드에 장소 관련 키워드가 없다면 검색 중심점 좌표를 기준으로 검색을 실시합니다.
                    
                    현재 페이지 1개, 페이지당 15개의 장소가 검색되고 있으며 정확도 순으로 정렬됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공: 지도 장소 검색 조회 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 - 필수 파라미터 누락 또는 잘못된 형식"),
            @ApiResponse(responseCode = "502", description = "카카오 API 서버 통신 오류")
    })
    ResponseEntity<CommonResponse<MapSearchResponse>> getMapSearch(
            @Parameter(description = "검색할 키워드 (예: 카페, 맛집, 병원)", required = true, example = "카페")
            @RequestParam String query,
            
            @Parameter(description = "검색 중심점 경도 (longitude)", required = true, example = "126.9780")
            @RequestParam Double lon,
            
            @Parameter(description = "검색 중심점 위도 (latitude)", required = true, example = "37.5665")
            @RequestParam Double lat
    );

    @Operation(
            summary = "경로 GPX 파일 다운로드",
            description = """
                    특정 경로의 GPX 파일을 다운로드합니다.
                    
                    - GPX 파일은 GPS 추적 데이터를 포함한 표준 형식입니다.
                    - 대부분의 GPS 앱과 호환됩니다.
                    - 파일명: {경로제목}.gpx
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공: GPX 파일 다운로드 완료"),
            @ApiResponse(responseCode = "404", description = "경로를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "GPX 파일 생성 중 오류가 발생했습니다."),
    })
    ResponseEntity<byte[]> downloadGpxFile(
            @Parameter(description = "경로 ID")
            @PathVariable String routeId
    );

    @Operation(
            summary = "내 경로에 추가",
            description = "공유받은 경로를 내 경로 목록에 추가합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공: 경로 추가 완료"),
            @ApiResponse(responseCode = "404", description = "경로를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "409", description = "이미 추가된 경로입니다."),
    })
    ResponseEntity<CommonResponse<Void>> addRouteToMyRoutes(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody AddRouteToMyRoutesRequest request
    );

    @Operation(
            summary = "추천 코스 복사",
            description = "추천 코스를 내 경로로 복사합니다. 새로운 경로가 생성되며 모든 GPS 로그가 복사됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "성공: 경로 복사 완료"),
            @ApiResponse(responseCode = "404", description = "경로를 찾을 수 없습니다."),
    })
    ResponseEntity<CommonResponse<CreateRouteResponse>> copyRecommendedRoute(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CopyRecommendedRouteRequest request
    );
}