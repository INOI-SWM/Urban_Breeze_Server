package com.ridingmate.api_server.domain.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Apple HealthKit 연동 응답 DTO
 */
public record AppleConnectResponse(
        @Schema(description = "연동 성공 여부", example = "true")
        Boolean isConnected,

        @Schema(description = "연동 일시", example = "2024-01-15T10:30:00")
        LocalDateTime connectedAt,

        @Schema(description = "마지막 동기화 일시", example = "2024-01-15T10:30:00")
        LocalDateTime lastSyncAt,

        @Schema(description = "연동 상태 메시지", example = "Apple HealthKit이 성공적으로 연동되었습니다.")
        String message
) {
    public static AppleConnectResponse from(com.ridingmate.api_server.domain.user.entity.AppleUser appleUser) {
        return new AppleConnectResponse(
                appleUser.isActive(),
                appleUser.getCreatedAt(),
                appleUser.getLastSyncDate(),
                "Apple HealthKit이 성공적으로 연동되었습니다."
        );
    }
}
