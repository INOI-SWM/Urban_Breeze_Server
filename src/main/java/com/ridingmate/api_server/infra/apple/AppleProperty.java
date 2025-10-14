package com.ridingmate.api_server.infra.apple;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Apple 관련 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "apple")
public record AppleProperty(
        
        @NotBlank
        String jwksUrl,
        
        @NotBlank
        String clientId,
        
        @NotBlank
        String issuer
) {
}