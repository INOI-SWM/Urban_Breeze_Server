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

    private String secretKey;

    private long accessTokenExpireTime;

    private long refreshTokenExpireTime;

    private String issuer;

    private Map<String, String> oauth2ClientIds = new HashMap<>();

    public String getClientId(String providerCode) {
        return oauth2ClientIds.get(providerCode);
    }
} 