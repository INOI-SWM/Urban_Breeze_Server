package com.ridingmate.api_server.domain.auth.controller;

import com.ridingmate.api_server.domain.auth.dto.request.AppleLoginRequest;
import com.ridingmate.api_server.domain.auth.dto.request.GoogleLoginRequest;
import com.ridingmate.api_server.domain.auth.dto.request.KakaoLoginRequest;
import com.ridingmate.api_server.domain.auth.dto.request.RefreshTokenRequest;
import com.ridingmate.api_server.domain.auth.dto.response.LoginResponse;
import com.ridingmate.api_server.global.exception.CommonResponse;
import com.ridingmate.api_server.domain.auth.dto.TokenInfo;
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

    @Operation(
            summary = "토큰 갱신",
            description = """
            리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급받습니다.
            
            **Token Rotation 방식:**
            - 기존 리프레시 토큰은 사용됨 처리되어 재사용 불가
            - 새로운 액세스 토큰과 리프레시 토큰이 함께 발급
            - 보안을 위해 반드시 새로운 리프레시 토큰을 저장해야 함
            
            **보안 기능:**
            - 이미 사용된 토큰 재사용 시 토큰 패밀리 전체 무효화
            - 토큰 탈취 감지 시 자동 보안 조치
            
            - refreshToken: 현재 유효한 리프레시 토큰
            - 응답: 새로운 액세스 토큰 + 새로운 리프레시 토큰
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공: 토큰 갱신 완료"),
            @ApiResponse(responseCode = "401", description = "인증 실패 - 유효하지 않은 리프레시 토큰"),
            @ApiResponse(responseCode = "401", description = "AUTH201: 유효하지 않은 리프레시 토큰"),
            @ApiResponse(responseCode = "401", description = "AUTH202: 만료된 리프레시 토큰"),
            @ApiResponse(responseCode = "401", description = "AUTH203: 이미 사용된 리프레시 토큰 (보안 위반 - 토큰 패밀리 무효화)"),
            @ApiResponse(responseCode = "401", description = "AUTH204: 무효화된 리프레시 토큰 (보안 위반)"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 - 필수 파라미터 누락")
    })
    ResponseEntity<CommonResponse<TokenInfo>> refreshToken(@Valid @RequestBody RefreshTokenRequest request);
} 