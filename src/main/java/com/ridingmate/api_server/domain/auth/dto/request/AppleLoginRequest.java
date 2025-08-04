package com.ridingmate.api_server.domain.auth.dto.request;

import com.ridingmate.api_server.global.security.enums.SocialProvider;

public record AppleLoginRequest(
    String idToken
) implements SocialLoginRequest {
    
    @Override
    public SocialProvider getProvider() {
        return SocialProvider.APPLE;
    }

    @Override
    public String getIdToken() {
        return idToken;
    }
} 