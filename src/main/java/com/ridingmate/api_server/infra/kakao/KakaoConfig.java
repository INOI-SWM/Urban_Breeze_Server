package com.ridingmate.api_server.infra.kakao;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(KakaoProperty.class)
@RequiredArgsConstructor
public class KakaoConfig {

    private final KakaoProperty kakaoProperty;

    @Bean
    public WebClient kakaoWebClient() {
        return WebClient.builder()
                .baseUrl(kakaoProperty.baseUrl())
                .defaultHeader("Authorization", kakaoProperty.apiKey())
                .build();
    }

    @Bean
    public KakaoClient kakaoClient(WebClient kakaoWebClient) {
        return new KakaoClient(kakaoWebClient);
    }
} 