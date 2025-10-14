package com.ridingmate.api_server.infra.kakao;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao")
public record KakaoProperty(
        @NotBlank
        String apiKey,
        @NotBlank
        String baseUrl
) {
} 