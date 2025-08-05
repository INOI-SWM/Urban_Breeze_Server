package com.ridingmate.api_server.global.security.dto;

import com.ridingmate.api_server.global.security.enums.SocialProvider;
import lombok.Builder;
import lombok.Getter;

/**
 * Google 사용자 정보 DTO
 */
@Getter
@Builder
public class GoogleUserInfo implements SocialUserInfo {
    
    private String userId;        // Google 사용자 ID
    private String email;         // 이메일
    private String name;          // 이름 (null 가능)
    private String picture;       // 프로필 이미지 URL (null 가능)

    /**
     * 필수 정보 검증
     */
    public boolean isValid() {
        return userId != null && !userId.isEmpty() && 
               email != null && !email.isEmpty();
    }

    // SocialUserInfo 인터페이스 구현
    @Override
    public SocialProvider getProvider() {
        return SocialProvider.GOOGLE;
    }

    @Override
    public String getSocialId() {
        return userId;
    }

    @Override
    public String getNickname() {
        return name != null ? name : "사용자";
    }

    @Override
    public String getProfileImageUrl() {
        return picture;
    }
} 