package com.ridingmate.api_server.domain.auth.jwt;

import com.ridingmate.api_server.domain.auth.config.JwtProperties;
import com.ridingmate.api_server.domain.auth.dto.AuthUserInfo;
import com.ridingmate.api_server.domain.auth.dto.TokenInfo;
import com.ridingmate.api_server.domain.auth.security.CustomUserDetailsService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey jwtSecretKey;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * AuthUserInfo 정보로부터 JWT 액세스 토큰 생성
     * @param authUserInfo 인증된 사용자 정보 DTO
     * @return JWT 액세스 토큰 문자열
     */
    public String generateAccessToken(AuthUserInfo authUserInfo) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpireTime());

        // 사용자 식별자로 provider:socialId 형식 사용
        String subject = authUserInfo.getProvider() + ":" + authUserInfo.getSocialId();

        return Jwts.builder()
                .subject(subject)                                        // 사용자 식별자로 provider:socialId 사용
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
     * @param authUserInfo 인증된 사용자 정보 DTO
     * @return TokenInfo DTO
     */
    public TokenInfo generateTokenInfo(AuthUserInfo authUserInfo) {
        String accessToken = generateAccessToken(authUserInfo);
        String refreshToken = generateRefreshToken(authUserInfo.getUserId());

        return TokenInfo.bearer(
            accessToken,
            refreshToken,
            jwtProperties.getAccessTokenExpireTime() / 1000  // 초 단위로 변환
        );
    }

    /**
     * JWT 토큰에서 Authentication 객체 생성 (DB 조회 방식)
     * @param token JWT 토큰 문자열
     * @return Spring Security Authentication 객체
     */
    public Authentication getAuthentication(String token) {
        String subject = parseClaims(token).getSubject();
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(subject);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    /**
     * JWT 토큰 유효성 검증
     * JWT 예외들을 JwtExceptionFilter에서 처리할 수 있도록 그대로 던짐
     * @param token JWT 토큰 문자열
     * @return 유효하면 true
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("만료된 JWT 토큰 - JwtExceptionFilter로 전달: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.debug("지원되지 않는 JWT 토큰 - JwtExceptionFilter로 전달: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.debug("잘못된 형식의 JWT 토큰 - JwtExceptionFilter로 전달: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.debug("잘못된 JWT 서명 - JwtExceptionFilter로 전달: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.debug("JWT 토큰이 비어있음 - JwtExceptionFilter로 전달: {}", e.getMessage());
            throw e;
        }
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