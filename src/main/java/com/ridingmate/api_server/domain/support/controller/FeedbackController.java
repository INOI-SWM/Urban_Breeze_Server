package com.ridingmate.api_server.domain.support.controller;

import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.domain.support.dto.request.CreateFeedbackRequest;
import com.ridingmate.api_server.domain.support.dto.response.FeedbackResponse;
import com.ridingmate.api_server.domain.support.exception.FeedbackSuccessCode;
import com.ridingmate.api_server.domain.support.exception.code.FeedbackErrorCode;
import com.ridingmate.api_server.domain.support.facade.FeedbackFacade;
import com.ridingmate.api_server.global.exception.ApiErrorCodeExample;
import com.ridingmate.api_server.global.exception.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController implements FeedbackApi {

    private final FeedbackFacade feedbackFacade;

    @Override
    @PostMapping
    @ApiErrorCodeExample(FeedbackErrorCode.class)
    public ResponseEntity<CommonResponse<FeedbackResponse>> createFeedback(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody CreateFeedbackRequest request
    ) {
        FeedbackResponse response = feedbackFacade.createFeedback(authUser.id(), request);
        return ResponseEntity
                .status(FeedbackSuccessCode.FEEDBACK_CREATED.getStatus())
                .body(CommonResponse.success(FeedbackSuccessCode.FEEDBACK_CREATED, response));
    }
}