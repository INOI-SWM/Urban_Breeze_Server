package com.ridingmate.api_server.domain.activity.controller;

import com.ridingmate.api_server.domain.activity.dto.request.ActivityListRequest;
import com.ridingmate.api_server.domain.activity.dto.response.ActivityDetailResponse;
import com.ridingmate.api_server.domain.activity.dto.response.ActivityListResponse;
import com.ridingmate.api_server.domain.activity.exception.ActivitySuccessCode;
import com.ridingmate.api_server.domain.activity.facade.ActivityFacade;
import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.global.exception.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController implements ActivityApi {

    private final ActivityFacade activityFacade;

    @GetMapping
    @Override
    public ResponseEntity<CommonResponse<ActivityListResponse>> getActivityList(
            @AuthenticationPrincipal AuthUser authUser,
            @ModelAttribute ActivityListRequest request
    ) {
        ActivityListResponse response = activityFacade.getActivityList(authUser, request);
        return ResponseEntity
                .status(ActivitySuccessCode.ACTIVITY_LIST_FETCHED.getStatus())
                .body(CommonResponse.success(ActivitySuccessCode.ACTIVITY_LIST_FETCHED, response));
    }

    @GetMapping("/{activityId}")
    @Override
    public ResponseEntity<CommonResponse<ActivityDetailResponse>> getActivityDetail(
            @PathVariable Long activityId
    ) {
        ActivityDetailResponse response = activityFacade.getActivityDetail(activityId);
        return ResponseEntity
                .status(ActivitySuccessCode.ACTIVITY_DETAIL_FETCHED.getStatus())
                .body(CommonResponse.success(ActivitySuccessCode.ACTIVITY_DETAIL_FETCHED, response));
    }
}
