package com.ridingmate.api_server.domain.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Apple HealthKit 운동 기록 업로드 응답 DTO
 */
public record AppleWorkoutImportResponse(
        @Schema(description = "생성된 주행 기록 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        String activityId,

        @Schema(description = "운동 기록 제목", example = "자전거 타기")
        String title,

        @Schema(description = "운동 시작 시간", example = "2024-01-15T10:30:00")
        LocalDateTime startTime,

        @Schema(description = "운동 종료 시간", example = "2024-01-15T11:30:00")
        LocalDateTime endTime,

        @Schema(description = "운동 거리 (미터)", example = "12000.0")
        Double distance,

        @Schema(description = "운동 소요 시간 (초)", example = "3600")
        Long durationSeconds,

        @Schema(description = "소모 칼로리", example = "450.0")
        Double calories,

        @Schema(description = "평균 심박수 (bpm)", example = "140")
        Integer averageHeartRate,

        @Schema(description = "최대 심박수 (bpm)", example = "165")
        Integer maxHeartRate,

        @Schema(description = "평균 케이던스 (rpm)", example = "85")
        Integer averageCadence,

        @Schema(description = "평균 파워 (W)", example = "180")
        Integer averagePower,

        @Schema(description = "총 상승 고도 (미터)", example = "150.0")
        Double elevationGain,

        @Schema(description = "GPS 로그 개수", example = "1200")
        Integer gpsLogCount,

        @Schema(description = "썸네일 이미지 경로", example = "thumbnails/activity_123.jpg")
        String thumbnailImagePath
) {
    /**
     * Activity 엔티티로부터 응답 생성
     */
    public static AppleWorkoutImportResponse from(com.ridingmate.api_server.domain.activity.entity.Activity activity, Integer gpsLogCount) {
        return new AppleWorkoutImportResponse(
                activity.getActivityId().toString(),
                activity.getTitle(),
                activity.getStartedAt(),
                activity.getEndedAt(),
                activity.getDistance(),
                activity.getDuration() != null ? activity.getDuration().getSeconds() : null,
                activity.getCalories(), // Activity 엔티티의 칼로리 정보
                activity.getAverageHeartRate(),
                activity.getMaxHeartRate(),
                activity.getCadence(),
                activity.getAveragePower(),
                activity.getElevationGain(),
                gpsLogCount,
                activity.getThumbnailImagePath()
        );
    }
}
