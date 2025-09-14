package com.ridingmate.api_server.infra.apple;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(AppleProperty.class)
@RequiredArgsConstructor
public class AppleConfig {

    private final AppleProperty appleProperty;

    @Bean
    @Qualifier("appleWebClient")
    public WebClient appleWebClient() {
        return WebClient.builder()
                .baseUrl(appleProperty.jwksUrl().substring(0, appleProperty.jwksUrl().lastIndexOf("/")))
                .build();
    }

    @Bean
    public AppleClient appleClient(@Qualifier("appleWebClient") WebClient appleWebClient) {
        return new AppleClient(appleWebClient);
    }
}
