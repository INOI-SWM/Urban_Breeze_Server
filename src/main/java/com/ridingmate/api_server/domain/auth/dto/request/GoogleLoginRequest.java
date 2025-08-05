package com.ridingmate.api_server.domain.auth.dto.request;

import com.ridingmate.api_server.global.security.enums.SocialProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Google 로그인 요청 DTO
 */
public record GoogleLoginRequest(
    @Schema(description = "Google ID 토큰", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE...")
    @NotBlank(message = "ID 토큰은 필수입니다.")
    String idToken
) implements SocialLoginRequest {
    
    @Override
    public SocialProvider getProvider() {
        return SocialProvider.GOOGLE;
    }

    @Override
    public String getIdToken() {
        return idToken;
    }
}
