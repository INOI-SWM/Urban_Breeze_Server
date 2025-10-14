package com.ridingmate.api_server.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Kakao 로그인 요청 DTO
 */
public record KakaoLoginRequest(
    @Schema(description = "Kakao Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @NotBlank(message = "Access Token은 필수입니다.")
    String accessToken
) {} 