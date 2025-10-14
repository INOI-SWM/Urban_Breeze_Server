package com.ridingmate.api_server.domain.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Apple HealthKit 여러 운동 기록 업로드 응답 DTO
 */
public record AppleWorkoutsImportResponse(
        @Schema(description = "업로드된 운동 기록 목록")
        List<AppleWorkoutImportResponse> activities,

        @Schema(description = "총 업로드된 운동 기록 수", example = "5")
        Integer totalCount,

        @Schema(description = "성공적으로 업로드된 운동 기록 수", example = "5")
        Integer successCount,

        @Schema(description = "실패한 운동 기록 수", example = "0")
        Integer failureCount
) {
    /**
     * 응답 생성
     */
    public static AppleWorkoutsImportResponse of(List<AppleWorkoutImportResponse> activities) {
        int totalCount = activities.size();
        int successCount = activities.size(); // 현재는 모두 성공으로 가정
        int failureCount = 0;

        return new AppleWorkoutsImportResponse(activities, totalCount, successCount, failureCount);
    }
}
