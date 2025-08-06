package com.ridingmate.api_server.domain.auth.dto;

import com.ridingmate.api_server.domain.auth.enums.SocialProvider;

/**
 * 소셜 로그인 사용자 정보 공통 인터페이스
 */
public interface SocialUserInfo {
    
    /**
     * 소셜 provider 반환
     */
    SocialProvider getProvider();
    
    /**
     * 소셜 고유 ID 반환
     */
    String getSocialId();
    
    /**
     * 이메일 반환
     */
    String getEmail();
    
    /**
     * 닉네임 반환
     */
    String getNickname();
    
    /**
     * 프로필 이미지 URL 반환
     */
    String getProfileImageUrl();
} 