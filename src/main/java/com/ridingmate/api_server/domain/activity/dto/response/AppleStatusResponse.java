package com.ridingmate.api_server.domain.activity.dto.response;

import com.ridingmate.api_server.domain.user.entity.AppleUser;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Apple HealthKit 연동 상태 조회 응답 DTO
 */
public record AppleStatusResponse(
        @Schema(description = "연동 여부", example = "true")
        Boolean isConnected,

        @Schema(description = "연동 일시", example = "2024-01-15T10:30:00")
        LocalDateTime connectedAt,

        @Schema(description = "마지막 동기화 일시", example = "2024-01-15T10:30:00")
        LocalDateTime lastSyncAt,

        @Schema(description = "연동 상태 메시지", example = "Apple HealthKit이 연동되어 있습니다.")
        String message
) {
    public static AppleStatusResponse from(AppleUser appleUser) {
        return new AppleStatusResponse(
                appleUser.isActive(),
                appleUser.getCreatedAt(),
                appleUser.getLastSyncDate(),
                "Apple HealthKit이 연동되어 있습니다."
        );
    }

    public static AppleStatusResponse notConnected() {
        return new AppleStatusResponse(
                false,
                null,
                null,
                "Apple HealthKit이 연동되어 있지 않습니다."
        );
    }
}
