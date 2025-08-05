package com.ridingmate.api_server.global.security.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 소셜 로그인 Provider Enum
 */
@Getter
@RequiredArgsConstructor
public enum SocialProvider {
    GOOGLE("google", "Google"),
    APPLE("apple", "Apple"),
    KAKAO("kakao", "Kakao");

    private final String code;
    private final String displayName;

    /**
     * 코드로부터 SocialProvider 찾기
     */
    public static SocialProvider fromCode(String code) {
        for (SocialProvider provider : values()) {
            if (provider.getCode().equals(code)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 소셜 provider: " + code);
    }
} 