package com.ridingmate.api_server.global.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app")
public record AppConfigProperties(
        @NotBlank
        String scheme
) {
}
