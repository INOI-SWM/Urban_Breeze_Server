package com.ridingmate.api_server.domain.auth.dto.request;

import com.ridingmate.api_server.global.security.enums.SocialProvider;

/**
 * 소셜 로그인 요청 공통 인터페이스
 */
public interface SocialLoginRequest {
    
    /**
     * 소셜 provider 반환
     */
    SocialProvider getProvider();
    
    /**
     * ID 토큰 반환
     */
    String getIdToken();
} 