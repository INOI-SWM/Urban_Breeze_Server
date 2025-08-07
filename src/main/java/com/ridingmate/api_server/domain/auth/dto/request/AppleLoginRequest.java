package com.ridingmate.api_server.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Apple 로그인 요청 DTO
 */
public record AppleLoginRequest(
    @Schema(description = "Apple ID 토큰", example = "eyJraWQ6IjEyMzQ1Njc4OTAiLCJhbGciOiJSUzI1NiJ9...")
    @NotBlank(message = "ID 토큰은 필수입니다.")
    String idToken
) {} 