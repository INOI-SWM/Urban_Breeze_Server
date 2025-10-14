package com.ridingmate.api_server.domain.activity.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Apple HealthKit 여러 운동 기록 업로드 요청 DTO
 */
public record AppleWorkoutsImportRequest(
        @Schema(description = "운동 기록 목록")
        @NotEmpty(message = "운동 기록 목록은 필수입니다.")
        @Valid
        List<AppleWorkoutImportRequest> workouts
) {}
