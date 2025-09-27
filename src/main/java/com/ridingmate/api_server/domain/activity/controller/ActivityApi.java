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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Activity", description = "주행 기록 관련 API")
public interface ActivityApi {

    @Operation(
            summary = "주행 기록 목록 조회",
            description = "사용자의 주행 기록 목록을 페이징하여 조회합니다.\n\n" +
                    "정렬 옵션:\n" +
                    "- STARTED_AT_DESC: 최신순\n" +
                    "- STARTED_AT_ASC: 오래된순\n" +
                    "- DISTANCE_ASC: 주행거리 오름차순\n" +
                    "- DISTANCE_DESC: 주행거리 내림차순"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "주행 기록 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ActivityListResponse.class))
            ),
    })
    ResponseEntity<CommonResponse<ActivityListResponse>> getActivityList(
             @AuthenticationPrincipal AuthUser authUser,
             @ModelAttribute ActivityListRequest request
    );

    @Operation(
            summary = "주행 기록 상세 조회",
            description = "특정 주행 기록의 상세 정보를 조회합니다. GPS 좌표, 고도 프로필, 이미지 등 모든 정보를 포함합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "주행 기록 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = ActivityDetailResponse.class))
            ),
    })
    ResponseEntity<CommonResponse<ActivityDetailResponse>> getActivityDetail(
            @Parameter(description = "조회할 주행 기록 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String activityId
    );

    @Operation(
            summary = "주행 기록 통계 조회",
            description = "사용자의 주행 기록 통계를 기간별(주간/월간/연간)로 조회합니다. 첫 번째 주행 기록부터 현재까지의 통계를 제공합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "주행 기록 통계 조회 성공",
                    content = @Content(schema = @Schema(implementation = ActivityStatsResponse.class))
            ),
    })
    ResponseEntity<CommonResponse<ActivityStatsResponse>> getActivityStats(
            @AuthenticationPrincipal AuthUser authUser,
            @ModelAttribute ActivityStatsRequest request
    );



    @Operation(
            summary = "주행 기록 제목 변경",
            description = "주행 기록의 제목을 변경합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "제목 변경 성공",
                    content = @Content(schema = @Schema(implementation = UpdateActivityTitleResponse.class))
            ),
    })
    ResponseEntity<CommonResponse<UpdateActivityTitleResponse>> updateActivityTitle(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "주행 기록 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String activityId,
            @Parameter(description = "제목 변경 요청")
            @RequestBody UpdateActivityTitleRequest request
    );

    @Operation(
            summary = "주행 기록 이미지 업로드",
            description = "주행 기록에 새로운 이미지들을 업로드합니다. 여러 개의 이미지를 한 번에 업로드할 수 있으며, 표시 순서는 업로드 순서대로 자동 할당됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "이미지 업로드 성공",
                    content = @Content(schema = @Schema(implementation = UploadActivityImagesResponse.class))
            ),
    })
    ResponseEntity<CommonResponse<UploadActivityImagesResponse>> uploadActivityImages(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "주행 기록 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String activityId,
            @Parameter(description = "업로드할 이미지 파일들 (표시 순서는 업로드 순서대로 자동 할당)")
            @RequestPart List<MultipartFile> files
    );

    @Operation(
            summary = "주행 기록 이미지 삭제",
            description = "주행 기록의 특정 이미지를 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "이미지 삭제 성공",
                    content = @Content(schema = @Schema(implementation = DeleteActivityImageResponse.class))
            ),
    })
    ResponseEntity<CommonResponse<DeleteActivityImageResponse>> deleteActivityImage(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "주행 기록 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String activityId,
            @Parameter(description = "삭제할 이미지 ID", example = "1")
            @PathVariable Long imageId
    );

    @Operation(
            summary = "주행 기록 삭제",
            description = "특정 주행 기록을 삭제합니다. 관련된 모든 데이터(이미지, GPS 로그)도 함께 삭제됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공: 주행 기록 삭제 완료"),
            @ApiResponse(responseCode = "404", description = "주행 기록을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "403", description = "해당 주행 기록에 대한 권한이 없습니다."),
    })
    ResponseEntity<CommonResponse<Void>> deleteActivity(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "삭제할 주행 기록 ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String activityId
    );
}
