package com.ridingmate.api_server.infra.apple;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "apple")
public record AppleProperty(
        @NotBlank
        String clientId,
        
        @NotBlank
        String privateKey,
        
        @NotBlank
        String keyId,
        
        @NotBlank
        String teamId
) {
} 