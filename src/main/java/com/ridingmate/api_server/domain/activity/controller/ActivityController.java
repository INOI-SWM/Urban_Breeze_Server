package com.ridingmate.api_server.domain.activity.controller;

import com.ridingmate.api_server.domain.activity.dto.request.ActivityListRequest;
import com.ridingmate.api_server.domain.activity.dto.request.ActivityStatsRequest;
import com.ridingmate.api_server.domain.activity.dto.request.UpdateActivityTitleRequest;
import com.ridingmate.api_server.domain.activity.dto.response.ActivityDetailResponse;
import com.ridingmate.api_server.domain.activity.dto.response.ActivityListResponse;
import com.ridingmate.api_server.domain.activity.dto.response.ActivityStatsResponse;
import com.ridingmate.api_server.domain.activity.dto.response.DeleteActivityImageResponse;
import com.ridingmate.api_server.domain.activity.dto.response.UpdateActivityTitleResponse;
import com.ridingmate.api_server.domain.activity.dto.response.UploadActivityImagesResponse;
import com.ridingmate.api_server.domain.activity.exception.ActivitySuccessCode;
import com.ridingmate.api_server.domain.activity.facade.ActivityFacade;
import com.ridingmate.api_server.domain.auth.security.AuthUser;
import com.ridingmate.api_server.global.annotation.FormDataRequestBody;
import com.ridingmate.api_server.global.exception.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @GetMapping("/stats")
    @Override
    public ResponseEntity<CommonResponse<ActivityStatsResponse>> getActivityStats(
            @AuthenticationPrincipal AuthUser authUser,
            @ModelAttribute ActivityStatsRequest request
    ) {
        ActivityStatsResponse response = activityFacade.getActivityStats(authUser, request);
        return ResponseEntity
                .status(ActivitySuccessCode.ACTIVITY_STATS_FETCHED.getStatus())
                .body(CommonResponse.success(ActivitySuccessCode.ACTIVITY_STATS_FETCHED, response));
    }



    @PostMapping(value = "/{activityId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public ResponseEntity<CommonResponse<UploadActivityImagesResponse>> uploadActivityImages(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long activityId,
            @RequestPart List<MultipartFile> files
    ) {
        UploadActivityImagesResponse response = activityFacade.uploadActivityImages(authUser, activityId, files);
        return ResponseEntity
                .status(ActivitySuccessCode.ACTIVITY_IMAGE_ADDED.getStatus())
                .body(CommonResponse.success(ActivitySuccessCode.ACTIVITY_IMAGE_ADDED, response));
    }

    @DeleteMapping("/{activityId}/images/{imageId}")
    @Override
    public ResponseEntity<CommonResponse<DeleteActivityImageResponse>> deleteActivityImage(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long activityId,
            @PathVariable Long imageId
    ) {
        DeleteActivityImageResponse response = activityFacade.deleteActivityImage(authUser, activityId, imageId);
        return ResponseEntity
                .status(ActivitySuccessCode.ACTIVITY_IMAGE_DELETED.getStatus())
                .body(CommonResponse.success(ActivitySuccessCode.ACTIVITY_IMAGE_DELETED, response));
    }

    @PutMapping("/{activityId}/title")
    @Override
    public ResponseEntity<CommonResponse<UpdateActivityTitleResponse>> updateActivityTitle(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long activityId,
            @RequestBody @Valid UpdateActivityTitleRequest request
    ) {
        UpdateActivityTitleResponse response = activityFacade.updateActivityTitle(authUser, activityId, request);
        return ResponseEntity
                .status(ActivitySuccessCode.ACTIVITY_TITLE_UPDATED.getStatus())
                .body(CommonResponse.success(ActivitySuccessCode.ACTIVITY_TITLE_UPDATED, response));
    }

    @DeleteMapping("/{activityId}")
    @Override
    public ResponseEntity<CommonResponse<Void>> deleteActivity(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long activityId
    ) {
        activityFacade.deleteActivity(authUser, activityId);
        return ResponseEntity
                .status(ActivitySuccessCode.ACTIVITY_DELETED.getStatus())
                .body(CommonResponse.success(ActivitySuccessCode.ACTIVITY_DELETED));
    }
}
