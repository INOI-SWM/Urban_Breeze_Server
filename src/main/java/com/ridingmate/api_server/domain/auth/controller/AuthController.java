package com.ridingmate.api_server.domain.auth.controller;

import com.ridingmate.api_server.domain.auth.dto.request.AppleLoginRequest;
import com.ridingmate.api_server.domain.auth.dto.request.GoogleLoginRequest;
import com.ridingmate.api_server.domain.auth.dto.request.KakaoLoginRequest;
import com.ridingmate.api_server.domain.auth.dto.request.RefreshTokenRequest;
import com.ridingmate.api_server.domain.auth.dto.response.LoginResponse;
import com.ridingmate.api_server.domain.auth.exception.AuthSuccessCode;
import com.ridingmate.api_server.domain.auth.facade.AuthFacade;
import com.ridingmate.api_server.global.exception.CommonResponse;
import com.ridingmate.api_server.domain.auth.dto.TokenInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthFacade authFacade;

    @Override
    @PostMapping("/google/login")
    public ResponseEntity<CommonResponse<LoginResponse>> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        log.info("Google 로그인 요청");
        LoginResponse response = authFacade.googleLogin(request);
        return ResponseEntity
                .status(AuthSuccessCode.GOOGLE_LOGIN_SUCCESS.getStatus())
                .body(CommonResponse.success(AuthSuccessCode.GOOGLE_LOGIN_SUCCESS, response));
    }

    @Override
    @PostMapping("/apple/login")
    public ResponseEntity<CommonResponse<LoginResponse>> appleLogin(@Valid @RequestBody AppleLoginRequest request) {
        log.info("Apple 로그인 요청");
        LoginResponse response = authFacade.appleLogin(request);
        return ResponseEntity
                .status(AuthSuccessCode.APPLE_LOGIN_SUCCESS.getStatus())
                .body(CommonResponse.success(AuthSuccessCode.APPLE_LOGIN_SUCCESS, response));
    }

    @Override
    @PostMapping("/kakao/login")
    public ResponseEntity<CommonResponse<LoginResponse>> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        log.info("Kakao 로그인 요청");
        LoginResponse response = authFacade.kakaoLogin(request);
        return ResponseEntity
                .status(AuthSuccessCode.KAKAO_LOGIN_SUCCESS.getStatus())
                .body(CommonResponse.success(AuthSuccessCode.KAKAO_LOGIN_SUCCESS, response));
    }

    /**
     * 토큰 갱신 API
     */
    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<TokenInfo>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("토큰 갱신 요청");
        TokenInfo response = authFacade.refreshAccessToken(request.refreshToken());
        return ResponseEntity
                .status(AuthSuccessCode.TOKEN_REFRESH_SUCCESS.getStatus())
                .body(CommonResponse.success(AuthSuccessCode.TOKEN_REFRESH_SUCCESS, response));
    }
} 