package com.ridingmate.api_server.domain.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * OAuth2 설정 프로퍼티
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "oauth2")
public class OAuth2Properties {

    /**
     * Google Client ID 목록 (iOS, Android 등)
     */
    private List<String> googleClientIds;

    /**
     * Apple Client ID (단일 - iOS/Android 공통)
     */
    private String appleClientId;

    /**
     * Kakao Client ID (단일 - iOS/Android 공통)
     */
    private String kakaoClientId;

    /**
     * Google Client ID 목록 반환
     */
    public List<String> getGoogleClientIds() {
        return googleClientIds;
    }

    /**
     * 첫 번째 Google Client ID 반환 (하위 호환성)
     */
    public String getGoogleClientId() {
        return googleClientIds != null && !googleClientIds.isEmpty() 
            ? googleClientIds.get(0) 
            : null;
    }

    /**
     * Apple Client ID 반환
     */
    public String getAppleClientId() {
        return appleClientId;
    }

    /**
     * Kakao Client ID 반환
     */
    public String getKakaoClientId() {
        return kakaoClientId;
    }
} 