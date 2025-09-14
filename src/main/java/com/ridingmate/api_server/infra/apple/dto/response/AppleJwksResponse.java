package com.ridingmate.api_server.infra.apple.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Apple JWKS 엔드포인트 응답 DTO
 */
public record AppleJwksResponse(
        @JsonProperty("keys")
        List<JwkKey> keys
) {
    
    /**
     * JWK (JSON Web Key) 정보
     */
    public record JwkKey(
            @JsonProperty("kty") // Key Type (RSA)
            String keyType,
            
            @JsonProperty("kid") // Key ID
            String keyId,
            
            @JsonProperty("use") // Public Key Use (sig)
            String use,
            
            @JsonProperty("alg") // Algorithm (RS256)
            String algorithm,
            
            @JsonProperty("n") // RSA public key modulus (Base64URL-encoded)
            String modulus,
            
            @JsonProperty("e") // RSA public key exponent (Base64URL-encoded)
            String exponent
    ) {
        
        /**
         * RSA 키인지 확인
         */
        public boolean isRsaKey() {
            return "RSA".equals(keyType);
        }
        
        /**
         * 서명용 키인지 확인
         */
        public boolean isSignatureKey() {
            return "sig".equals(use);
        }
        
        /**
         * RS256 알고리즘인지 확인
         */
        public boolean isRs256Algorithm() {
            return "RS256".equals(algorithm);
        }
        
        /**
         * 유효한 서명 검증용 키인지 확인
         */
        public boolean isValidForSignatureVerification() {
            return isRsaKey() && isSignatureKey() && isRs256Algorithm();
        }
    }
}
