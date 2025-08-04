package com.ridingmate.api_server.domain.auth.facade;

import com.ridingmate.api_server.domain.auth.dto.request.AppleLoginRequest;
import com.ridingmate.api_server.domain.auth.dto.request.GoogleLoginRequest;
import com.ridingmate.api_server.domain.auth.dto.request.KakaoLoginRequest;
import com.ridingmate.api_server.domain.auth.dto.response.LoginResponse;
import com.ridingmate.api_server.domain.auth.service.TokenService;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.service.UserService;
import com.ridingmate.api_server.global.security.dto.GoogleUserInfo;
import com.ridingmate.api_server.global.security.dto.TokenInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final TokenService tokenService;
    private final UserService userService;

    /**
     * Google 로그인 처리
     *
     * @param request Google 로그인 요청
     * @return LoginResponse 로그인 응답
     */
    public LoginResponse googleLogin(GoogleLoginRequest request) {
        // 1. Google ID 토큰 검증

        GoogleUserInfo googleUserInfo = tokenService.verifyGoogleToken(request.getIdToken());
        
        // 2. 사용자 조회 또는 생성
        User user = userService.findOrCreateUser(googleUserInfo);
        
        // 3. JWT 토큰 생성
        TokenInfo tokenInfo = tokenService.generateToken(user);
        
        // 4. 응답 생성
        return createLoginResponse(tokenInfo, user);
    }

    /**
     * Apple 로그인 처리
     *
     * @param request Apple 로그인 요청
     * @return LoginResponse 로그인 응답
     */
    public LoginResponse appleLogin(AppleLoginRequest request) {
        // TODO: Apple ID 토큰 검증 구현
        throw new UnsupportedOperationException("Apple 로그인은 아직 지원되지 않습니다.");
    }

    /**
     * Kakao 로그인 처리
     *
     * @param request Kakao 로그인 요청
     * @return LoginResponse 로그인 응답
     */
    public LoginResponse kakaoLogin(KakaoLoginRequest request) {
        // TODO: Kakao ID 토큰 검증 구현
        throw new UnsupportedOperationException("Kakao 로그인은 아직 지원되지 않습니다.");
    }

    /**
     * 로그인 응답 생성
     */
    private LoginResponse createLoginResponse(TokenInfo tokenInfo, User user) {
        return new LoginResponse(
            tokenInfo,
            new LoginResponse.UserInfo(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImagePath()
            )
        );
    }
}
