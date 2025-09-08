package com.ridingmate.api_server.domain.activity.dto.request;

import com.ridingmate.api_server.infra.terra.TerraProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record IntegrationProviderAuthRequest(

        @Schema(description = "기록 연동을 위해 인증 진행할 서비스", example = "STRAVA")
        @NotNull
        TerraProvider terraProvider
) {
}
