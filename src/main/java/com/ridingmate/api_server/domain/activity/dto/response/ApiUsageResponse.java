package com.ridingmate.api_server.domain.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * API 사용량 조회 응답 DTO
 */
public record ApiUsageResponse(
        @Schema(description = "현재 월 총 사용량", example = "15")
        Integer currentUsage,

        @Schema(description = "월별 제한", example = "30")
        Integer monthlyLimit,

        @Schema(description = "남은 사용량", example = "15")
        Integer remainingUsage,

        @Schema(description = "제한 초과 여부", example = "false")
        Boolean isExceeded
) {
    /**
     * 사용량 정보로부터 응답 생성
     */
    public static ApiUsageResponse of(Integer currentUsage, Integer monthlyLimit) {
        Integer remainingUsage = Math.max(0, monthlyLimit - currentUsage);
        Boolean isExceeded = currentUsage >= monthlyLimit;

        return new ApiUsageResponse(
                currentUsage,
                monthlyLimit,
                remainingUsage,
                isExceeded
        );
    }
}
