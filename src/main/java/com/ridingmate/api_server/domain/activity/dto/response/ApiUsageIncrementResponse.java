package com.ridingmate.api_server.domain.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * API 사용량 증가 응답 DTO
 */
public record ApiUsageIncrementResponse(
        @Schema(description = "현재 시간", example = "2024-01-15T10:30:00")
        LocalDateTime currentTime,

        @Schema(description = "증가된 사용량", example = "16")
        Integer currentUsage,

        @Schema(description = "월별 제한", example = "30")
        Integer monthlyLimit,

        @Schema(description = "남은 사용량", example = "14")
        Integer remainingUsage,

        @Schema(description = "제한 초과 여부", example = "false")
        Boolean isExceeded
) {
    /**
     * 사용량 정보로부터 응답 생성
     */
    public static ApiUsageIncrementResponse of(Integer currentUsage, Integer monthlyLimit) {
        LocalDateTime currentTime = LocalDateTime.now();
        Integer remainingUsage = Math.max(0, monthlyLimit - currentUsage);
        Boolean isExceeded = currentUsage >= monthlyLimit;

        return new ApiUsageIncrementResponse(
                currentTime,
                currentUsage,
                monthlyLimit,
                remainingUsage,
                isExceeded
        );
    }
}
