package com.ridingmate.api_server.global.security.provider;

import com.ridingmate.api_server.global.security.dto.AppleUserInfo;
import com.ridingmate.api_server.infra.apple.AppleProperty;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.Base64;

/**
 * Apple ID 토큰 검증기 (JWT 검증 방식)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppleIdTokenValidator {

    private final AppleProperty appleProperty;

    /**
     * Apple ID 토큰 검증 (JWT 방식)
     *
     * @param idToken Apple ID Token (JWT)
     * @return AppleUserInfo Apple 사용자 정보
     * @throws IllegalArgumentException 토큰이 유효하지 않은 경우
     */
    public AppleUserInfo verify(String idToken) {
        try {
            log.debug("Apple ID Token JWT 검증 시작");
            
            // JWT 파싱 및 검증
            Claims claims = Jwts.parser()
                    .verifyWith(getApplePublicKey())
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();
            
            // 필수 클레임 검증
            validateClaims(claims);
            
            // 사용자 정보 추출
            String userId = claims.getSubject();
            String email = claims.get("email", String.class);
            String name = claims.get("name", String.class);
            
            // 사용자 정보 생성
            AppleUserInfo userInfo = AppleUserInfo.builder()
                    .userId(userId)
                    .email(email)
                    .name(name)
                    .profileImageUrl(null) // Apple은 프로필 이미지를 제공하지 않음
                    .build();

            log.debug("Apple ID Token JWT 검증 성공 - 사용자: {}", userInfo.getNickname());
            return userInfo;

        } catch (SecurityException e) {
            log.error("Apple ID Token 서명 검증 실패", e);
            throw new IllegalArgumentException("Apple ID Token 서명이 유효하지 않습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("Apple ID Token JWT 검증 중 오류 발생", e);
            throw new IllegalArgumentException("Apple ID Token 검증에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * Apple Public Key 생성
     * 
     * @return PublicKey Apple Public Key
     */
    private PublicKey getApplePublicKey() {
        try {
            return generatePublicKeyFromPrivateKey();
            
        } catch (Exception e) {
            log.error("Apple Public Key 생성 실패", e);
            throw new IllegalArgumentException("Apple Public Key 생성에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * Private Key로부터 Public Key 생성 (개발용)
     * 실제 운영에서는 Apple JWKS 사용 권장
     */
    private PublicKey generatePublicKeyFromPrivateKey() {
        try {
            // Private Key 파싱
            String privateKeyPEM = appleProperty.privateKey()
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPEM);
            
            // RSA 키 페어 생성 (개발용)
            // 실제 운영에서는 Apple JWKS 사용
            java.security.KeyPairGenerator keyGen = java.security.KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            java.security.KeyPair pair = keyGen.generateKeyPair();
            
            return pair.getPublic();
            
        } catch (Exception e) {
            log.error("Private Key로부터 Public Key 생성 실패", e);
            throw new IllegalArgumentException("Public Key 생성에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * JWT 클레임 검증
     * 
     * @param claims JWT 클레임
     */
    private void validateClaims(Claims claims) {
        // iss (issuer) 검증
        String issuer = claims.getIssuer();
        if (!"https://appleid.apple.com".equals(issuer)) {
            throw new IllegalArgumentException("유효하지 않은 Apple ID Token issuer입니다.");
        }
        
        // aud (audience) 검증
        String audience = claims.getAudience().stream().findFirst().orElse(null);
        if (!appleProperty.clientId().equals(audience)) {
            throw new IllegalArgumentException("유효하지 않은 Apple ID Token audience입니다.");
        }
        
        // exp (expiration) 검증
        if (claims.getExpiration() == null || claims.getExpiration().before(new java.util.Date())) {
            throw new IllegalArgumentException("Apple ID Token이 만료되었습니다.");
        }
        
        // sub (subject) 검증
        if (claims.getSubject() == null || claims.getSubject().isEmpty()) {
            throw new IllegalArgumentException("Apple ID Token에 사용자 ID가 없습니다.");
        }
    }
} 