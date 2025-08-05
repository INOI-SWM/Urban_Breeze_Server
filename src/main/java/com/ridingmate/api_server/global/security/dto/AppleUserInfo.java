package com.ridingmate.api_server.global.security.dto;

import com.ridingmate.api_server.global.security.enums.SocialProvider;
import lombok.Builder;
import lombok.Getter;

/**
 * Apple 사용자 정보 DTO
 */
@Getter
@Builder
public class AppleUserInfo implements SocialUserInfo {
    
    private String userId;        // Apple 사용자 ID (sub)
    private String email;         // 이메일 (null 가능 - 사용자가 거부할 수 있음)
    private String name;          // 이름 (null 가능 - 사용자가 거부할 수 있음)
    private String profileImageUrl; // 프로필 이미지 URL (Apple은 제공하지 않음)

    /**
     * 필수 정보 검증
     * Apple은 이메일과 이름을 선택적으로 제공하므로 userId만 필수
     */
    public boolean isValid() {
        return userId != null && !userId.isEmpty();
    }

    // SocialUserInfo 인터페이스 구현
    @Override
    public SocialProvider getProvider() {
        return SocialProvider.APPLE;
    }

    @Override
    public String getSocialId() {
        return userId;
    }

    @Override
    public String getNickname() {
        return name != null ? name : "Apple 사용자";
    }

    @Override
    public String getProfileImageUrl() {
        return profileImageUrl; // Apple은 프로필 이미지를 제공하지 않음
    }

    /**
     * Private Email Relay 여부 확인
     * Apple의 프라이버시 기능으로 실제 이메일 대신 relay 이메일 사용
     */
    public boolean isPrivateEmailRelay() {
        return email != null && email.endsWith("@privaterelay.appleid.com");
    }

    /**
     * 이메일이 제공되었는지 확인
     */
    public boolean hasEmail() {
        return email != null && !email.isEmpty();
    }

    /**
     * 이름이 제공되었는지 확인
     */
    public boolean hasName() {
        return name != null && !name.isEmpty();
    }
} 