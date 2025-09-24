package com.ridingmate.api_server.domain.auth.facade;

import com.ridingmate.api_server.domain.auth.dto.request.AppleLoginRequest;
import com.ridingmate.api_server.domain.auth.dto.request.GoogleLoginRequest;
import com.ridingmate.api_server.domain.auth.dto.request.KakaoLoginRequest;
import com.ridingmate.api_server.domain.auth.dto.response.LoginResponse;
import com.ridingmate.api_server.domain.auth.service.AgreementService;
import com.ridingmate.api_server.domain.auth.service.RefreshTokenService;
import com.ridingmate.api_server.domain.auth.service.TokenService;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.service.UserService;
import com.ridingmate.api_server.domain.auth.dto.AppleUserInfo;
import com.ridingmate.api_server.domain.auth.dto.GoogleUserInfo;
import com.ridingmate.api_server.domain.auth.dto.KakaoUserInfo;
import com.ridingmate.api_server.domain.auth.dto.TokenInfo;
import com.ridingmate.api_server.domain.auth.dto.AgreementStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final TokenService tokenService;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final AgreementService agreementService;

    /**
     * Google 로그인 처리
     *
     * @param request Google 로그인 요청
     * @return LoginResponse 로그인 응답
     */
    public LoginResponse googleLogin(GoogleLoginRequest request) {
        // 1. Google ID 토큰 검증
        GoogleUserInfo googleUserInfo = tokenService.verifyGoogleToken(request.idToken());
        
        // 2. 사용자 조회 또는 생성
        User user = userService.findOrCreateUser(googleUserInfo);
        
        // 3. 동의항목 상태 조회
        AgreementStatusResponse agreementStatus = agreementService.getAgreementStatus(user);
        
        // 4. JWT 토큰 생성
        TokenInfo tokenInfo = tokenService.generateToken(user);
        
        // 5. 응답 생성
        return LoginResponse.of(tokenInfo, user, agreementStatus);
    }

    /**
     * Apple 로그인 처리
     *
     * @param request Apple 로그인 요청
     * @return LoginResponse 로그인 응답
     */
    public LoginResponse appleLogin(AppleLoginRequest request) {
        // 1. Apple ID 토큰 검증
        AppleUserInfo appleUserInfo = tokenService.verifyAppleToken(request.idToken());

        // 2. 사용자 조회 또는 생성
        User user = userService.findOrCreateUser(appleUserInfo);

        // 3. 동의항목 상태 조회
        AgreementStatusResponse agreementStatus = agreementService.getAgreementStatus(user);

        // 4. JWT 토큰 생성
        TokenInfo tokenInfo = tokenService.generateToken(user);

        // 5. 응답 생성
        return LoginResponse.of(tokenInfo, user, agreementStatus);
    }

    /**
     * Kakao 로그인 처리
     *
     * @param request Kakao 로그인 요청
     * @return LoginResponse 로그인 응답
     */
    public LoginResponse kakaoLogin(KakaoLoginRequest request) {
        // 1. Kakao Access Token 검증
        KakaoUserInfo kakaoUserInfo = tokenService.verifyKakaoToken(request.accessToken());
        
        // 2. 사용자 조회 또는 생성
        User user = userService.findOrCreateUser(kakaoUserInfo);
        
        // 3. 동의항목 상태 조회
        AgreementStatusResponse agreementStatus = agreementService.getAgreementStatus(user);
        
        // 4. JWT 토큰 생성
        TokenInfo tokenInfo = tokenService.generateToken(user);
        
        // 5. 응답 생성
        return LoginResponse.of(tokenInfo, user, agreementStatus);
    }

    public TokenInfo refreshAccessToken(String refreshToken){
        return refreshTokenService.refreshAccessToken(refreshToken);
    }
}
