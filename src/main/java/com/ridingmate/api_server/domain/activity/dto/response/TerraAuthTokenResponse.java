package com.ridingmate.api_server.domain.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Terra 인증 토큰 발급 응답 DTO
 */
public record TerraAuthTokenResponse(
        @Schema(description = "Terra 인증 토큰", example = "250c68b9c21b78e40e7a3285a2d538d3bc24aabd3b4c76a782fb0a571")
        String token,

        @Schema(description = "토큰 만료 시간 (초)", example = "180")
        Integer expiresIn,

        @Schema(description = "상태", example = "success")
        String status
) {
    /**
     * Terra API 응답으로부터 생성
     */
    public static TerraAuthTokenResponse from(String token, Integer expiresIn, String status) {
        return new TerraAuthTokenResponse(token, expiresIn, status);
    }
}
