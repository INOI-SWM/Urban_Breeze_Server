package com.ridingmate.api_server.domain.auth.controller;

import com.ridingmate.api_server.domain.auth.dto.request.AppleLoginRequest;
import com.ridingmate.api_server.domain.auth.dto.request.GoogleLoginRequest;
import com.ridingmate.api_server.domain.auth.dto.request.KakaoLoginRequest;
import com.ridingmate.api_server.domain.auth.dto.response.LoginResponse;
import com.ridingmate.api_server.global.exception.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "인증 API", description = "소셜 로그인 API")
public interface AuthApi {

    @Operation(
            summary = "Google 로그인",
            description = """
            Google ID 토큰을 통해 사용자 인증을 수행합니다.
            
            - idToken: Google에서 발급받은 ID 토큰
            - 응답: JWT 액세스 토큰, 리프레시 토큰, 사용자 정보
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공: Google 로그인 완료"),
            @ApiResponse(responseCode = "401", description = "인증 실패 - 유효하지 않은 ID 토큰"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 - 필수 파라미터 누락")
    })
    ResponseEntity<CommonResponse<LoginResponse>> googleLogin(@Valid @RequestBody GoogleLoginRequest request);

    @Operation(
            summary = "Apple 로그인",
            description = """
            Apple ID 토큰을 통해 사용자 인증을 수행합니다.
            
            - idToken: Apple에서 발급받은 ID 토큰
            - 응답: JWT 액세스 토큰, 리프레시 토큰, 사용자 정보
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공: Apple 로그인 완료"),
            @ApiResponse(responseCode = "401", description = "인증 실패 - 유효하지 않은 ID 토큰"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 - 필수 파라미터 누락")
    })
    ResponseEntity<CommonResponse<LoginResponse>> appleLogin(@Valid @RequestBody AppleLoginRequest request);

    @Operation(
            summary = "Kakao 로그인",
            description = """
            Kakao ID 토큰을 통해 사용자 인증을 수행합니다.
            
            - idToken: Kakao에서 발급받은 ID 토큰
            - 응답: JWT 액세스 토큰, 리프레시 토큰, 사용자 정보
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공: Kakao 로그인 완료"),
            @ApiResponse(responseCode = "401", description = "인증 실패 - 유효하지 않은 ID 토큰"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 - 필수 파라미터 누락")
    })
    ResponseEntity<CommonResponse<LoginResponse>> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request);
} 