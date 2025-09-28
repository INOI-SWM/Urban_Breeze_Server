package com.ridingmate.api_server.domain.activity.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Apple HealthKit 운동 기록 업로드 요청 DTO
 */
public record AppleWorkoutImportRequest(
        @Schema(description = "외부 ID (Apple HealthKit UUID)", example = "apple-health-uuid-12345")
        String externalId,

        @Schema(description = "운동 시작 시간", example = "2024-01-15T10:30:00.000Z")
        @NotNull(message = "운동 시작 시간은 필수입니다.")
        LocalDateTime startTime,

        @Schema(description = "운동 종료 시간", example = "2024-01-15T11:30:00.000Z")
        @NotNull(message = "운동 종료 시간은 필수입니다.")
        LocalDateTime endTime,

        @Schema(description = "운동 소요 시간 (초)", example = "3600")
        @Positive(message = "운동 소요 시간은 양수여야 합니다.")
        Long duration,

        @Schema(description = "운동 거리 (미터)", example = "15000.0")
        @Positive(message = "운동 거리는 양수여야 합니다.")
        Double distance,

        @Schema(description = "소모 칼로리", example = "450.0")
        Double calories,

        @Schema(description = "데이터 소스", example = "apple_health_kit")
        String source,

        @Schema(description = "운동 기록 제목", example = "한강 라이딩")
        @NotBlank(message = "운동 기록 제목은 필수입니다.")
        String title,

        @Schema(description = "심박수 샘플 데이터")
        @Valid
        List<HeartRateSample> heartRateData,

        @Schema(description = "위치 데이터")
        @Valid
        List<LocationData> locationData
) {
    /**
     * Duration 객체로 변환
     */
    public Duration getDuration() {
        return duration != null ? Duration.ofSeconds(duration) : null;
    }

    /**
     * 심박수 샘플 데이터
     */
    public record HeartRateSample(
            @Schema(description = "기록 시간", example = "2024-01-15T10:30:00.000Z")
            @NotNull(message = "기록 시간은 필수입니다.")
            LocalDateTime timestamp,

            @Schema(description = "심박수 (bpm)", example = "120")
            @NotNull(message = "심박수는 필수입니다.")
            Integer heartRate
    ) {}

    /**
     * 위치 데이터
     */
    public record LocationData(
            @Schema(description = "위도", example = "37.5665")
            @NotNull(message = "위도는 필수입니다.")
            Double latitude,

            @Schema(description = "경도", example = "126.9780")
            @NotNull(message = "경도는 필수입니다.")
            Double longitude,

            @Schema(description = "기록 시간", example = "2024-01-15T10:30:00.000Z")
            @NotNull(message = "기록 시간은 필수입니다.")
            LocalDateTime timestamp,

            @Schema(description = "고도 (미터)", example = "50.0")
            Double altitude,

            @Schema(description = "속도 (m/s)", example = "5.5")
            Double speed,

            @Schema(description = "수평 정확도 (미터)", example = "5.0")
            Double horizontalAccuracy,

            @Schema(description = "수직 정확도 (미터)", example = "3.0")
            Double verticalAccuracy,

            @Schema(description = "방향 (도)", example = "180.5")
            Double course
    ) {}
}
