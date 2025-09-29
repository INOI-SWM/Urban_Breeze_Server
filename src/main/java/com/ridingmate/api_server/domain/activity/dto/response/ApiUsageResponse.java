package com.ridingmate.api_server.domain.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

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
        Boolean isExceeded,

        @Schema(description = "제공자 별 마지막 갱신 일자")
        List<ProviderSyncInfo> providerSyncInfos
) {


    /**
     * 사용량 정보와 제공자 정보로부터 응답 생성
     */
    public static ApiUsageResponse of(Integer currentUsage, Integer monthlyLimit, List<ProviderSyncInfo> providerSyncInfos) {
        Integer remainingUsage = Math.max(0, monthlyLimit - currentUsage);
        Boolean isExceeded = currentUsage >= monthlyLimit;

        return new ApiUsageResponse(
                currentUsage,
                monthlyLimit,
                remainingUsage,
                isExceeded,
                providerSyncInfos
        );
    }

}
