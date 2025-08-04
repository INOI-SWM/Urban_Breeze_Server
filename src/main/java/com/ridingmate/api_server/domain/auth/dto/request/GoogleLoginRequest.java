package com.ridingmate.api_server.domain.auth.dto.request;

import com.ridingmate.api_server.global.security.enums.SocialProvider;

public record GoogleLoginRequest(
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
