package com.ridingmate.api_server.global.security.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialProvider {
    
    /**
     * 구글 소셜 로그인
     */
    GOOGLE("google", "Google"),
    
    /**
     * 애플 소셜 로그인
     */
    APPLE("apple", "Apple"),
    
    /**
     * 카카오 소셜 로그인
     */
    KAKAO("kakao", "Kakao");
    
    /**
     * API 경로나 설정에서 사용할 소문자 코드
     */
    private final String code;
    
    /**
     * 사용자에게 표시할 이름
     */
    private final String displayName;
    
    /**
     * 코드로 SocialProvider 찾기
     * @param code 소셜 제공자 코드 (예: "google")
     * @return SocialProvider enum
     * @throws IllegalArgumentException 지원하지 않는 제공자인 경우
     */
    public static SocialProvider fromCode(String code) {
        for (SocialProvider provider : values()) {
            if (provider.getCode().equalsIgnoreCase(code)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자입니다: " + code);
    }
} 