package com.ridingmate.api_server.domain.support.controller;

import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.domain.support.dto.request.CreateFeedbackRequest;
import com.ridingmate.api_server.domain.support.dto.response.FeedbackResponse;
import com.ridingmate.api_server.global.exception.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Feedback API", description = "피드백 기능 API")
public interface FeedbackApi {

    @Operation(
            summary = "피드백 등록",
            description = """
                    사용자가 피드백을 등록합니다.
                    
                    - 피드백 내용만 입력하면 됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "성공: 피드백이 등록되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청: 입력값이 유효하지 않습니다."),
    })
    ResponseEntity<CommonResponse<FeedbackResponse>> createFeedback(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody CreateFeedbackRequest request
    );
}