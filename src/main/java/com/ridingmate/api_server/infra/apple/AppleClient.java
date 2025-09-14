package com.ridingmate.api_server.infra.apple;

import com.ridingmate.api_server.global.exception.BusinessException;
import com.ridingmate.api_server.infra.apple.dto.response.AppleJwksResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.codec.CodecException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Apple API 클라이언트
 */
@Slf4j
@RequiredArgsConstructor
public class AppleClient {
    
    private final WebClient appleWebClient;

    /**
     * Apple JWKS 정보 조회 (캐시 적용)
     * 캐시 TTL: 1시간 (Apple의 키 로테이션 주기 고려)
     */
    @Cacheable(value = "appleJwks", unless = "#result == null")
    public AppleJwksResponse getJwks() {
        return appleWebClient.get()
                .uri("/auth/keys")
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            if (response.statusCode().is4xxClientError()) {
                                return Mono.error(new AppleException(AppleErrorCode.APPLE_REQUEST_FAILED));
                            }
                            return Mono.error(new AppleException(AppleErrorCode.APPLE_SERVER_ERROR));
                        })
                )
                .bodyToMono(AppleJwksResponse.class)
                .onErrorMap(
                    throwable -> !(throwable instanceof BusinessException),
                    throwable -> {
                        if (throwable instanceof CodecException) {
                            log.error("[Apple] JWKS Response Mapping Failed", throwable);
                            return new AppleException(AppleErrorCode.APPLE_JWKS_MAPPING_FAILED);
                        }
                        log.error("[Apple] JWKS Call - Network or Unknown Error", throwable);
                        return new AppleException(AppleErrorCode.APPLE_CONNECTION_FAILED);
                    }
                )
                .block();
    }

    /**
     * 특정 Key ID로 JWK 키 조회
     */
    public Optional<AppleJwksResponse.JwkKey> getJwkByKeyId(String keyId) {
        if (keyId == null || keyId.isBlank()) {
            log.warn("Key ID is null or blank");
            return Optional.empty();
        }

        AppleJwksResponse jwks = getJwks();
        if (jwks == null || jwks.keys() == null) {
            log.warn("JWKS response is null or empty");
            return Optional.empty();
        }

        return jwks.keys().stream()
                .filter(key -> keyId.equals(key.keyId()))
                .filter(AppleJwksResponse.JwkKey::isValidForSignatureVerification)
                .findFirst();
    }

    /**
     * JWK를 RSA PublicKey로 변환
     */
    public PublicKey convertJwkToPublicKey(AppleJwksResponse.JwkKey jwkKey) {
        try {
            // Base64URL 디코딩
            byte[] nBytes = Base64.getUrlDecoder().decode(jwkKey.modulus());
            byte[] eBytes = Base64.getUrlDecoder().decode(jwkKey.exponent());

            // BigInteger로 변환
            BigInteger modulus = new BigInteger(1, nBytes);
            BigInteger exponent = new BigInteger(1, eBytes);

            // RSA PublicKey 생성
            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            
            PublicKey publicKey = keyFactory.generatePublic(spec);
            log.debug("Successfully converted JWK to RSA PublicKey for keyId: {}", jwkKey.keyId());
            
            return publicKey;
            
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            log.error("[Apple] Key conversion failed for keyId: {}", jwkKey.keyId(), e);
            throw new AppleException(AppleErrorCode.APPLE_KEY_CONVERSION_FAILED);
        }
    }

    /**
     * Key ID로 RSA PublicKey 직접 조회
     */
    public Optional<PublicKey> getPublicKeyByKeyId(String keyId) {
        return getJwkByKeyId(keyId)
                .map(this::convertJwkToPublicKey);
    }

    /**
     * 사용 가능한 모든 Key ID 목록 조회
     */
    public List<String> getAvailableKeyIds() {
        AppleJwksResponse jwks = getJwks();
        if (jwks == null || jwks.keys() == null) {
            return List.of();
        }

        return jwks.keys().stream()
                .filter(AppleJwksResponse.JwkKey::isValidForSignatureVerification)
                .map(AppleJwksResponse.JwkKey::keyId)
                .toList();
    }
}
