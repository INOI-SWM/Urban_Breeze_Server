package com.ridingmate.api_server.infra.kakao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Qualifier("kakaoWebClient")
    public WebClient kakaoWebClient() {
        return WebClient.builder()
                .baseUrl(kakaoProperty.baseUrl())
                .defaultHeader("Authorization", "KakaoAK " + kakaoProperty.apiKey())
                .build();
    }

    @Bean
    @Qualifier("kakaoApiWebClient")
    public WebClient kakaoApiWebClient() {
        return WebClient.builder()
                .baseUrl("https://kapi.kakao.com")
                .build();
    }

    @Bean
    public KakaoClient kakaoClient(
        @Qualifier("kakaoWebClient") WebClient kakaoWebClient,
        @Qualifier("kakaoApiWebClient") WebClient kakaoApiWebClient) {
        return new KakaoClient(kakaoWebClient, kakaoApiWebClient);
    }
} 