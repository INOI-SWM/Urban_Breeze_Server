package com.ridingmate.api_server.global.security.provider;

import com.ridingmate.api_server.global.security.config.JwtProperties;
import com.ridingmate.api_server.global.security.dto.AuthUser;
import com.ridingmate.api_server.global.security.dto.TokenInfo;
import com.ridingmate.api_server.global.security.enums.SocialProvider;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    
    private final JwtProperties jwtProperties;
    private final SecretKey jwtSecretKey;
    
    /**
     * AuthUser 정보로부터 JWT 액세스 토큰 생성
     * @param authUser 인증된 사용자 정보
     * @return JWT 액세스 토큰 문자열
     */
    public String generateAccessToken(AuthUser authUser) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpireTime());
        
        return Jwts.builder()
                .subject(String.valueOf(authUser.getUserId()))           // 사용자 ID
                .claim("email", authUser.getEmail())                     // 이메일
                .claim("nickname", authUser.getNickname())               // 닉네임
                .claim("profileImageUrl", authUser.getProfileImageUrl()) // 프로필 이미지
                .claim("socialProvider", authUser.getProvider().getCode()) // 소셜 제공자
                .claim("socialId", authUser.getSocialId())               // 소셜 고유 ID
                .claim("type", "access")                                 // 토큰 타입
                .issuer(jwtProperties.getIssuer())                       // 발급자
                .issuedAt(now)                                           // 발급 시간
                .expiration(expireDate)                                  // 만료 시간
                .signWith(jwtSecretKey)                                  // 서명
                .compact();
    }
    
    /**
     * 리프레시 토큰 생성 (사용자 ID만 포함)
     * @param userId 사용자 ID
     * @return JWT 리프레시 토큰 문자열
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + jwtProperties.getRefreshTokenExpireTime());
        
        return Jwts.builder()
                .subject(String.valueOf(userId))                         // 사용자 ID만
                .claim("type", "refresh")                                // 토큰 타입
                .issuer(jwtProperties.getIssuer())                       // 발급자
                .issuedAt(now)                                           // 발급 시간
                .expiration(expireDate)                                  // 만료 시간
                .signWith(jwtSecretKey)                                  // 서명
                .compact();
    }
    
    /**
     * 토큰 정보 생성 (액세스 토큰 + 리프레시 토큰)
     * @param authUser 인증된 사용자 정보
     * @return TokenInfo DTO
     */
    public TokenInfo generateTokenInfo(AuthUser authUser) {
        String accessToken = generateAccessToken(authUser);
        String refreshToken = generateRefreshToken(authUser.getUserId());
        
        return TokenInfo.bearer(
            accessToken,
            refreshToken,
            jwtProperties.getAccessTokenExpireTime() / 1000  // 초 단위로 변환
        );
    }
    
    /**
     * JWT 토큰에서 Authentication 객체 생성
     * @param token JWT 토큰 문자열
     * @return Spring Security Authentication 객체
     */
    public Authentication getAuthentication(String token) {
        AuthUser authUser = getAuthUserFromToken(token);
        return new UsernamePasswordAuthenticationToken(authUser, token, authUser.getAuthorities());
    }
    
    /**
     * JWT 토큰에서 AuthUser 정보 추출
     * @param token JWT 토큰 문자열
     * @return AuthUser 객체
     */
    public AuthUser getAuthUserFromToken(String token) {
        Claims claims = parseClaims(token);
        
        return AuthUser.builder()
                .userId(Long.valueOf(claims.getSubject()))
                .email(claims.get("email", String.class))
                .nickname(claims.get("nickname", String.class))
                .profileImageUrl(claims.get("profileImageUrl", String.class))
                .provider(SocialProvider.fromCode(claims.get("socialProvider", String.class)))
                .socialId(claims.get("socialId", String.class))
                .build();
    }
    
    /**
     * JWT 토큰 유효성 검증
     * @param token JWT 토큰 문자열
     * @return 유효하면 true, 무효하면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("잘못된 형식의 JWT 토큰입니다: {}", e.getMessage());
        } catch (SecurityException | IllegalArgumentException e) {
            log.warn("잘못된 JWT 서명입니다: {}", e.getMessage());
        }
        return false;
    }
    
    /**
     * JWT 토큰에서 Claims 추출 (private 메서드)
     * @param token JWT 토큰 문자열
     * @return JWT Claims
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 만료된 토큰이어도 Claims는 추출 가능
        }
    }
} 