package com.ridingmate.api_server.global.security.dto;

import com.ridingmate.api_server.global.security.enums.SocialProvider;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoogleUserInfo implements SocialUserInfo {
    
    private String userId;        // Google 사용자 ID
    private String email;         // 이메일
    private String name;          // 이름 (null 가능)
    private String picture;       // 프로필 이미지 URL (null 가능)
    
    /**
     * Google 사용자 정보를 AuthUser로 변환
     *
     * @return AuthUser 인증 사용자 정보
     */
    public AuthUser toAuthUser() {
        return AuthUser.builder()
                .provider(SocialProvider.GOOGLE)
                .socialId(userId)
                .email(email)
                .nickname(name != null ? name : "사용자")  // null 처리
                .profileImageUrl(picture)  // null도 허용
                .build();
    }
    
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