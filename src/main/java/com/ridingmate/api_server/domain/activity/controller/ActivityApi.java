package com.ridingmate.api_server.domain.activity.controller;

import com.ridingmate.api_server.domain.activity.dto.request.ActivityListRequest;
import com.ridingmate.api_server.domain.activity.dto.response.ActivityListResponse;
import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.global.exception.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;

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
}
