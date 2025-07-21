package com.ridingmate.api_server.infra.ors;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(OrsProperty.class)
@RequiredArgsConstructor
public class OrsConfig {

    private final OrsProperty orsProperty;

    @Bean
    public WebClient orsWebClient() {
        return WebClient.builder()
                .baseUrl(orsProperty.baseUrl())
                .defaultHeader("Authorization", orsProperty.apiKey())
                .build();
    }

    @Bean
    public OrsClient orsClient(WebClient orsWebClient) {
        return new OrsClient(orsWebClient);
    }
} 