package com.ridingmate.api_server.infra.terra.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record TerraProviderAuthResponse(
    String status,

    @JsonProperty("user_id")
    UUID userId,

    @JsonProperty("auth_url")
    String authUrl
) {
}
