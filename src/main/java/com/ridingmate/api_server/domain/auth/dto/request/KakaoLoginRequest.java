package com.ridingmate.api_server.domain.auth.dto.request;

import com.ridingmate.api_server.global.security.enums.SocialProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Kakao 로그인 요청 DTO
 */
public record KakaoLoginRequest(
    @Schema(description = "Kakao ID 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @NotBlank(message = "ID 토큰은 필수입니다.")
    String idToken
) implements SocialLoginRequest {
    
    @Override
    public SocialProvider getProvider() {
        return SocialProvider.KAKAO;
    }

    @Override
    public String getIdToken() {
        return idToken;
    }
} 