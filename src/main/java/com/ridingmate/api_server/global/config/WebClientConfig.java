package com.ridingmate.api_server.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        // Terra에서 오는 큰 JSON 페이로드를 처리하기 위해 버퍼 사이즈를 늘립니다. (기본값: 256KB)
        final int size = 50 * 1024 * 1024; // 50MB
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                .build();

        return WebClient.builder()
                .exchangeStrategies(strategies)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(size))
                .build();
    }

}