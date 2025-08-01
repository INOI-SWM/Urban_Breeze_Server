package com.ridingmate.api_server.global.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    private String secretKey = "dGhpc2lzYXNlY3JldGtleWZvcnJpZGluZ21hdGVhcGlzZXJ2ZXJqdHRva2VuZ2VuZXJhdGlvbmFuZHZlcmlmaWNhdGlvbnRoaXNpc2F0bGVhc3QyNTZiaXRzCg==";

    private long accessTokenExpireTime = 3600000; // 1시간

    private long refreshTokenExpireTime = 604800000; // 7일

    private String issuer = "ridingmate-api";

    private Map<String, String> oauth2ClientIds = new HashMap<>();

    public String getClientId(String providerCode) {
        return oauth2ClientIds.get(providerCode);
    }
} 