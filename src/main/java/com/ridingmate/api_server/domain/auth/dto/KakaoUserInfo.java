package com.ridingmate.api_server.domain.auth.dto;

import com.ridingmate.api_server.domain.auth.enums.SocialProvider;
import lombok.Builder;
import lombok.Getter;

/**
 * Kakao 사용자 정보 DTO
 */
@Getter
@Builder
public class KakaoUserInfo implements SocialUserInfo {
    
    private String userId;        // Kakao 사용자 ID
    private String email;         // 이메일 (null 가능)
    private String nickname;      // 닉네임
    private String profileImageUrl; // 프로필 이미지 URL (null 가능)

    /**
     * 필수 정보 검증
     */
    public boolean isValid() {
        return userId != null && !userId.isEmpty() && 
               nickname != null && !nickname.isEmpty();
    }

    // SocialUserInfo 인터페이스 구현
    @Override
    public SocialProvider getProvider() {
        return SocialProvider.KAKAO;
    }

    @Override
    public String getSocialId() {
        return userId;
    }

    @Override
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
} 