package com.ridingmate.api_server.domain.auth.dto;

/**
 * JWT 토큰 정보 DTO
 */
public record TokenInfo(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresIn
) {

    /**
     * Bearer 토큰 정보 생성
     */
    public static TokenInfo bearer(String accessToken, String refreshToken, Long expiresIn) {
        return new TokenInfo(accessToken, refreshToken, "Bearer", expiresIn);
    }
} 