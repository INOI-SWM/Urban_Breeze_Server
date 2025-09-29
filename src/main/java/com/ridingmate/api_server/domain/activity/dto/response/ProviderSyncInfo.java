package com.ridingmate.api_server.domain.activity.dto.response;

import com.ridingmate.api_server.domain.user.entity.TerraUser;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ProviderSyncInfo(
        @Schema(description = "제공자 이름", example = "Samsung Health")
        String providerName,

        @Schema(description = "마지막 동기화 일시", example = "2024-01-15T10:30:00")
        LocalDateTime lastSyncAt,

        @Schema(description = "활성 상태", example = "true")
        Boolean isActive
) {
    public static ProviderSyncInfo from(TerraUser terraUser){
        return new ProviderSyncInfo(
                terraUser.getProvider().toString(),
                terraUser.getLastSyncDate(),
                terraUser.isActive()
        );
    }
}
