package com.ridingmate.api_server.domain.auth.service;

import com.ridingmate.api_server.domain.auth.dto.*;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.auth.validator.AppleIdTokenValidator;
import com.ridingmate.api_server.domain.auth.validator.GoogleIdTokenValidator;
import com.ridingmate.api_server.domain.auth.validator.KakaoIdTokenValidator;
import com.ridingmate.api_server.domain.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JWT 토큰 생성 및 검증 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleIdTokenValidator googleIdTokenValidator;
    private final KakaoIdTokenValidator kakaoIdTokenValidator;
    private final AppleIdTokenValidator appleIdTokenValidator;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public TokenInfo generateToken(User user) {
        // AuthUserInfo DTO를 사용하여 토큰 생성
        TokenInfo tokenInfo = jwtTokenProvider.generateTokenInfo(AuthUserInfo.from(user));
        // 리프레시 토큰 저장
        refreshTokenService.createRefreshToken(user, null, null);
        return tokenInfo;
    }

    /**
     * 토큰 유효성 검증
     *
     * @param token JWT 토큰
     * @return 유효성 여부
     */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * Google ID 토큰 검증
     *
     * @param idToken Google ID 토큰
     * @return GoogleUserInfo Google 사용자 정보
     */
    public GoogleUserInfo verifyGoogleToken(String idToken) {
        log.debug("Google ID 토큰 검증 시작");
        return googleIdTokenValidator.verify(idToken);
    }

    public AppleUserInfo verifyAppleToken(String idToken) {
        log.debug("Apple ID 토큰 검증 시작");
        return appleIdTokenValidator.verify(idToken);
    }

    /**
     * Kakao Access Token 검증
     *
     * @param accessToken Kakao Access Token
     * @return KakaoUserInfo Kakao 사용자 정보
     */
    public KakaoUserInfo verifyKakaoToken(String accessToken) {
        log.debug("Kakao Access Token 검증 시작");
        return kakaoIdTokenValidator.verify(accessToken);
    }

    /**
     * 사용자의 모든 Refresh Token 무효화 (로그아웃)
     *
     * @param user 사용자
     */
    public void revokeAllUserTokens(User user) {
        log.info("사용자 모든 토큰 무효화 - 사용자: {}", user.getId());
        refreshTokenService.revokeAllUserTokens(user);
    }
} 