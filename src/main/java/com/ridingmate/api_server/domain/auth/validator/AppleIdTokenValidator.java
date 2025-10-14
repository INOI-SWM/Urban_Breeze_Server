package com.ridingmate.api_server.domain.auth.validator;

import com.ridingmate.api_server.domain.auth.dto.AppleUserInfo;
import com.ridingmate.api_server.infra.apple.AppleClient;
import com.ridingmate.api_server.infra.apple.AppleErrorCode;
import com.ridingmate.api_server.infra.apple.AppleException;
import com.ridingmate.api_server.infra.apple.AppleProperty;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.Optional;

/**
 * Apple ID 토큰 검증기 (JWKS 기반 JWT 검증)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppleIdTokenValidator {

    private final AppleProperty appleProperty;
    private final AppleClient appleClient;

    /**
     * Apple ID 토큰 검증 (JWKS 기반)
     *
     * @param idToken Apple ID Token (JWT)
     * @return AppleUserInfo Apple 사용자 정보
     * @throws AppleException 토큰이 유효하지 않은 경우
     */
    public AppleUserInfo verify(String idToken) {
        try {
            log.debug("[Apple] ID Token JWT 검증 시작");
            
            // 1. JWT 헤더에서 Key ID 추출
            String keyId = extractKeyIdFromToken(idToken);
            log.debug("[Apple] Extracted Key ID: {}", keyId);
            
            // 2. Apple JWKS에서 공개키 조회
            PublicKey publicKey = getApplePublicKey(keyId);
            
            // 3. JWT 파싱 및 서명 검증
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();
            
            // 4. 필수 클레임 검증
            validateClaims(claims);
            
            // 5. 사용자 정보 추출
            String userId = claims.getSubject();
            String email = claims.get("email", String.class);
            Boolean emailVerified = claims.get("email_verified", Boolean.class);
            String name = extractName(claims);
            
            // 6. 사용자 정보 생성
            AppleUserInfo userInfo = AppleUserInfo.builder()
                    .userId(userId)
                    .email(email)
                    .name(name)
                    .profileImageUrl(null) // Apple은 프로필 이미지를 제공하지 않음
                    .build();

            log.debug("[Apple] ID Token JWT 검증 성공 - 사용자: {}", userId);
            return userInfo;

        } catch (AppleException e) {
            log.error("[Apple] ID Token 검증 실패", e);
            throw e;
        } catch (SecurityException e) {
            log.error("[Apple] ID Token 서명 검증 실패", e);
            throw new AppleException(AppleErrorCode.APPLE_REQUEST_FAILED);
        } catch (Exception e) {
            log.error("[Apple] ID Token JWT 검증 중 예상치 못한 오류 발생", e);
            throw new AppleException(AppleErrorCode.APPLE_REQUEST_FAILED);
        }
    }

    /**
     * JWT 헤더에서 Key ID 추출
     */
    private String extractKeyIdFromToken(String idToken) {
        try {
            // JWT 헤더만 파싱 (서명 검증 없이)
            String[] chunks = idToken.split("\\.");
            if (chunks.length != 3) {
                throw new AppleException(AppleErrorCode.APPLE_INVALID_KEY_ID);
            }
            
            // Base64URL 디코딩
            byte[] headerBytes = java.util.Base64.getUrlDecoder().decode(chunks[0]);
            String headerJson = new String(headerBytes);
            
            // JSON에서 kid 추출 (간단한 파싱)
            String keyId = extractKidFromHeaderJson(headerJson);
            if (keyId == null || keyId.isBlank()) {
                throw new AppleException(AppleErrorCode.APPLE_INVALID_KEY_ID);
            }
            
            return keyId;
            
        } catch (AppleException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Apple] JWT 헤더에서 Key ID 추출 실패", e);
            throw new AppleException(AppleErrorCode.APPLE_INVALID_KEY_ID);
        }
    }
    
    /**
     * JWT 헤더 JSON에서 kid 추출
     */
    private String extractKidFromHeaderJson(String headerJson) {
        try {
            // 간단한 JSON 파싱 (kid 값만 추출)
            if (headerJson.contains("\"kid\"")) {
                int kidIndex = headerJson.indexOf("\"kid\"");
                int colonIndex = headerJson.indexOf(":", kidIndex);
                int startQuote = headerJson.indexOf("\"", colonIndex);
                int endQuote = headerJson.indexOf("\"", startQuote + 1);
                
                if (startQuote != -1 && endQuote != -1) {
                    return headerJson.substring(startQuote + 1, endQuote);
                }
            }
            return null;
        } catch (Exception e) {
            log.error("[Apple] 헤더 JSON에서 kid 추출 실패: {}", headerJson, e);
            return null;
        }
    }

    /**
     * Apple JWKS에서 공개키 조회
     */
    private PublicKey getApplePublicKey(String keyId) {
        Optional<PublicKey> publicKey = appleClient.getPublicKeyByKeyId(keyId);
        
        if (publicKey.isEmpty()) {
            log.error("[Apple] Key ID에 해당하는 공개키를 찾을 수 없음: {}", keyId);
            throw new AppleException(AppleErrorCode.APPLE_INVALID_KEY_ID);
        }
        
        return publicKey.get();
    }

    /**
     * JWT Claims에서 사용자 이름 추출
     */
    private String extractName(Claims claims) {
        // Apple ID Token에서는 이름 정보가 다양한 형태로 제공될 수 있음
        String name = claims.get("name", String.class);
        if (name != null && !name.isBlank()) {
            return name;
        }
        
        // 이름이 없는 경우 이메일의 로컬 부분 사용
        String email = claims.get("email", String.class);
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        
        // 마지막으로 subject 사용
        return claims.getSubject();
    }

    /**
     * JWT 클레임 검증
     * 
     * @param claims JWT 클레임
     */
    private void validateClaims(Claims claims) {
        try {
            // iss (issuer) 검증
            String issuer = claims.getIssuer();
            if (!appleProperty.issuer().equals(issuer)) {
                log.error("[Apple] 잘못된 issuer: expected={}, actual={}", appleProperty.issuer(), issuer);
                throw new AppleException(AppleErrorCode.APPLE_REQUEST_FAILED);
            }
            
            // aud (audience) 검증
            String audience = claims.getAudience().stream().findFirst().orElse(null);
            if (!appleProperty.clientId().equals(audience)) {
                log.error("[Apple] 잘못된 audience: expected={}, actual={}", appleProperty.clientId(), audience);
                throw new AppleException(AppleErrorCode.APPLE_REQUEST_FAILED);
            }
            
            // exp (expiration) 검증 - JWT 파서가 자동으로 검증하지만 명시적 확인
            if (claims.getExpiration() == null || claims.getExpiration().before(new java.util.Date())) {
                log.error("[Apple] 만료된 토큰: expiration={}", claims.getExpiration());
                throw new AppleException(AppleErrorCode.APPLE_REQUEST_FAILED);
            }
            
            // iat (issued at) 검증 - 너무 오래된 토큰 거부
            if (claims.getIssuedAt() != null) {
                long issuedAtTime = claims.getIssuedAt().getTime();
                long currentTime = System.currentTimeMillis();
                long maxAge = 24 * 60 * 60 * 1000; // 24시간
                
                if (currentTime - issuedAtTime > maxAge) {
                    log.error("[Apple] 너무 오래된 토큰: issuedAt={}", claims.getIssuedAt());
                    throw new AppleException(AppleErrorCode.APPLE_REQUEST_FAILED);
                }
            }
            
            // sub (subject) 검증
            if (claims.getSubject() == null || claims.getSubject().isEmpty()) {
                log.error("[Apple] 사용자 ID가 없음: subject={}", claims.getSubject());
                throw new AppleException(AppleErrorCode.APPLE_REQUEST_FAILED);
            }
            
            log.debug("[Apple] 모든 클레임 검증 완료");
            
        } catch (AppleException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Apple] 클레임 검증 중 예상치 못한 오류", e);
            throw new AppleException(AppleErrorCode.APPLE_REQUEST_FAILED);
        }
    }
} 