package com.ridingmate.api_server.domain.auth.service;

import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.global.security.dto.AuthUser;
import com.ridingmate.api_server.global.security.dto.GoogleUserInfo;
import com.ridingmate.api_server.global.security.dto.KakaoUserInfo;
import com.ridingmate.api_server.global.security.dto.TokenInfo;
import com.ridingmate.api_server.global.security.provider.GoogleIdTokenValidator;
import com.ridingmate.api_server.global.security.provider.KakaoIdTokenValidator;
import com.ridingmate.api_server.global.security.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    /**
     * JWT 토큰 생성
     *
     * @param user 사용자 엔티티
     * @return TokenInfo JWT 토큰 정보
     */
    public TokenInfo generateToken(User user) {
        log.debug("JWT 토큰 생성 - 사용자: {}", user.getId());
        return jwtTokenProvider.generateTokenInfo(AuthUser.from(user));
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
     * 토큰에서 사용자 정보 추출
     *
     * @param token JWT 토큰
     * @return 사용자 정보
     */
    public User getUserFromToken(String token) {
        // TODO: 토큰에서 사용자 정보 추출 로직 구현
        return null;
    }
} 