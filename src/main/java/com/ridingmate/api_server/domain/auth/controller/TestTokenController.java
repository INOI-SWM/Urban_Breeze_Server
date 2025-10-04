package com.ridingmate.api_server.domain.auth.controller;

import com.ridingmate.api_server.domain.auth.dto.TokenInfo;
import com.ridingmate.api_server.domain.auth.exception.AuthSuccessCode;
import com.ridingmate.api_server.domain.auth.service.TokenService;
import com.ridingmate.api_server.domain.route.dto.response.GpxUploadResponse;
import com.ridingmate.api_server.domain.route.exception.RouteSuccessCode;
import com.ridingmate.api_server.domain.route.service.GpxRecommendationService;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.repository.UserRepository;
import com.ridingmate.api_server.global.exception.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 임시 토큰 생성 컨트롤러
 * 개발 초기 단계에서 사용
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Tag(name = "테스트 API", description = "개발용 테스트 API")
public class TestTokenController {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final GpxRecommendationService gpxRecommendationService;

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

    @Operation(
            summary = "GPX 파일로 추천코스 생성 (테스트용)",
            description = """
            GPX 파일을 업로드하여 추천코스를 생성합니다.
            
            **테스트용 기능:**
            - ID 1번 사용자로 자동 생성
            - GPX 파일 자동 파싱
            - 추천코스 데이터 생성
            - S3에 GPX 파일 저장
            
            **파일 제한:**
            - 최대 파일 크기: 10MB
            - 지원 형식: .gpx
            - 최소 좌표 수: 2개 이상
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "성공: 추천코스 생성 완료"),
            @ApiResponse(responseCode = "400", description = "실패: 잘못된 GPX 파일 또는 요청 데이터"),
            @ApiResponse(responseCode = "413", description = "실패: 파일 크기 초과")
    })
    @PostMapping(value = "/gpx/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<GpxUploadResponse>> uploadGpxFile(
            @Parameter(
                    description = "GPX 파일 (.gpx 형식, 최대 10MB)", 
                    required = true
            )
            @RequestParam("gpxFile") MultipartFile gpxFile,
            @Parameter(
                    description = "추천코스 제목", 
                    required = true
            )
            @RequestParam("title") String title,
            @Parameter(
                    description = "추천코스 설명"
            )
            @RequestParam(value = "description", required = false) String description,
            @Parameter(
                    description = "난이도", 
                    required = true
            )
            @RequestParam("difficulty") com.ridingmate.api_server.domain.route.enums.Difficulty difficulty,
            @Parameter(
                    description = "지역", 
                    required = true
            )
            @RequestParam("region") com.ridingmate.api_server.domain.route.enums.Region region,
            @Parameter(
                    description = "경관 타입", 
                    required = true
            )
            @RequestParam("landscapeType") com.ridingmate.api_server.domain.route.enums.LandscapeType landscapeType,
            @Parameter(
                    description = "추천 타입", 
                    required = true
            )
            @RequestParam("recommendationType") com.ridingmate.api_server.domain.route.enums.RecommendationType recommendationType
    ) {
        log.info("테스트 GPX 파일 업로드 요청: fileName={}, title={}", 
                gpxFile.getOriginalFilename(), title);

        // ID 1번 사용자 조회
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("ID 1번 사용자를 찾을 수 없습니다. 먼저 사용자를 생성해주세요."));

        // 파일 유효성 검사
        validateGpxFile(gpxFile);

        // GPX 파일로부터 추천코스 생성
        GpxUploadResponse response = gpxRecommendationService.createRecommendationFromGpx(
                user, gpxFile, title, description,
                difficulty, region, landscapeType, recommendationType
        );

        log.info("테스트 GPX 파일 업로드 완료: routeId={}", response.routeId());

        return ResponseEntity
                .status(RouteSuccessCode.GPX_UPLOAD_SUCCESS.getStatus())
                .body(CommonResponse.success(RouteSuccessCode.GPX_UPLOAD_SUCCESS, response));
    }

    /**
     * GPX 파일 유효성 검사
     */
    private void validateGpxFile(MultipartFile gpxFile) {
        if (gpxFile == null || gpxFile.isEmpty()) {
            throw new IllegalArgumentException("GPX 파일이 선택되지 않았습니다.");
        }

        if (!gpxFile.getOriginalFilename().toLowerCase().endsWith(".gpx")) {
            throw new IllegalArgumentException("GPX 파일만 업로드 가능합니다.");
        }

        // 파일 크기 제한 (10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (gpxFile.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }
    }

} 