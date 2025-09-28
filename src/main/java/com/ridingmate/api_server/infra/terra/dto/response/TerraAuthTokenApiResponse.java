package com.ridingmate.api_server.infra.terra.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Terra API 인증 토큰 발급 응답 DTO
 */
public record TerraAuthTokenApiResponse(
        @JsonProperty("status")
        String status,
        
        @JsonProperty("token")
        String token,
        
        @JsonProperty("expires_in")
        Integer expiresIn
) {}
