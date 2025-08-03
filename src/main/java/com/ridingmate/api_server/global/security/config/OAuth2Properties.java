package com.ridingmate.api_server.global.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt.oauth2-client-ids")
public class OAuth2Properties {
    
    private String google;
    private String apple;
    private String kakao;
    
    /**
     * Google 클라이언트 ID 조회
     */
    public String getGoogleClientId() {
        return google;
    }
    
    /**
     * Apple 클라이언트 ID 조회
     */
    public String getAppleClientId() {
        return apple;
    }
    
    /**
     * Kakao 클라이언트 ID 조회
     */
    public String getKakaoClientId() {
        return kakao;
    }
    
    /**
     * 특정 provider의 클라이언트 ID 조회
     */
    public String getClientId(String provider) {
        switch (provider.toLowerCase()) {
            case "google":
                return getGoogleClientId();
            case "apple":
                return getAppleClientId();
            case "kakao":
                return getKakaoClientId();
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }
} 