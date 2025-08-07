package com.ridingmate.api_server.domain.auth.controller;

import com.ridingmate.api_server.domain.auth.dto.TokenInfo;
import com.ridingmate.api_server.domain.auth.exception.AuthSuccessCode;
import com.ridingmate.api_server.domain.auth.service.TokenService;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.repository.UserRepository;
import com.ridingmate.api_server.global.exception.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 임시 토큰 생성 컨트롤러
 * 개발 초기 단계에서 사용
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Tag(name = "임시 토큰", description = "임시 토큰 생성 API (개발용)")
public class TestTokenController {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Operation(
            summary = "ID 1번 사용자 토큰 생성",
            description = """
            데이터베이스의 ID 1번 사용자로 JWT 토큰을 생성합니다.
            
            **개발 초기 단계용:**
            - 실제 인증 없이 토큰 생성
            - ID 1번 사용자 정보 사용
            - 프론트엔드 연동 테스트용
            
            **응답:**
            - Access Token
            - Refresh Token
            - 토큰 타입 및 만료 시간
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공: 토큰 생성 완료"),
            @ApiResponse(responseCode = "404", description = "실패: ID 1번 사용자를 찾을 수 없음")
    })
    @GetMapping("/generate-token")
    public ResponseEntity<CommonResponse<TokenInfo>> generateTokenForUser1() {
        log.info("ID 1번 사용자 토큰 생성 요청");
        
        // ID 1번 사용자 조회
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("ID 1번 사용자를 찾을 수 없습니다. 먼저 사용자를 생성해주세요."));
        
        // 토큰 생성
        TokenInfo tokenInfo = tokenService.generateToken(user);
        
        log.info("ID 1번 사용자 토큰 생성 완료 - 사용자: {}, 이메일: {}", user.getId(), user.getEmail());
        
        return ResponseEntity
                .status(AuthSuccessCode.TEST_LOGIN_SUCCESS.getStatus())
                .body(CommonResponse.success(AuthSuccessCode.KAKAO_LOGIN_SUCCESS, tokenInfo));
    }

} 