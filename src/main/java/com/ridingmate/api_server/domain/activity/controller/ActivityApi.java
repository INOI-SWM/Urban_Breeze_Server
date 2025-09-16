package com.ridingmate.api_server.domain.activity.controller;

import com.ridingmate.api_server.domain.activity.dto.request.ActivityListRequest;
import com.ridingmate.api_server.domain.activity.dto.request.ActivityStatsRequest;
import com.ridingmate.api_server.domain.activity.dto.response.ActivityDetailResponse;
import com.ridingmate.api_server.domain.activity.dto.response.ActivityListResponse;
import com.ridingmate.api_server.domain.activity.dto.response.ActivityStatsResponse;
import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.global.exception.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Activity", description = "활동 관련 API")
public interface ActivityApi {

    @Operation(
            summary = "활동 목록 조회",
            description = "사용자의 활동 목록을 페이징하여 조회합니다. 정렬 옵션: LATEST(최신순), OLDEST(오래된순)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "활동 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ActivityListResponse.class))
            ),
    })
    ResponseEntity<CommonResponse<ActivityListResponse>> getActivityList(
             @AuthenticationPrincipal AuthUser authUser,
             @ModelAttribute ActivityListRequest request
    );

    @Operation(
            summary = "활동 상세 조회",
            description = "특정 활동의 상세 정보를 조회합니다. GPS 좌표, 고도 프로필, 이미지 등 모든 정보를 포함합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "활동 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = ActivityDetailResponse.class))
            ),
    })
    ResponseEntity<CommonResponse<ActivityDetailResponse>> getActivityDetail(
            @Parameter(description = "조회할 활동 ID", example = "1")
            @PathVariable Long activityId
    );

    @Operation(
            summary = "활동 통계 조회",
            description = "사용자의 활동 통계를 기간별(주간/월간/연간)로 조회합니다. 첫 번째 활동부터 현재까지의 통계를 제공합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "활동 통계 조회 성공",
                    content = @Content(schema = @Schema(implementation = ActivityStatsResponse.class))
            ),
    })
    ResponseEntity<CommonResponse<ActivityStatsResponse>> getActivityStats(
            @AuthenticationPrincipal AuthUser authUser,
            @ModelAttribute ActivityStatsRequest request
    );
}
