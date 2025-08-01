package com.ridingmate.api_server.global.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo {

    private String accessToken;

    private String refreshToken;

    private String tokenType;

    private Long expiresIn;

    public static TokenInfo bearer(String accessToken, String refreshToken, Long expiresIn) {
        return TokenInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }
} 