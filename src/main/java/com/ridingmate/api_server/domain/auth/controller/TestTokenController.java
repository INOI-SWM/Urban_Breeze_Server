package com.ridingmate.api_server.domain.auth.controller;

import com.ridingmate.api_server.domain.auth.dto.TokenInfo;
import com.ridingmate.api_server.domain.auth.exception.AuthSuccessCode;
import com.ridingmate.api_server.domain.auth.service.TokenService;
import com.ridingmate.api_server.domain.privacy.service.LocationDataAccessLogService;
import com.ridingmate.api_server.domain.route.dto.response.GpxUploadResponse;
import com.ridingmate.api_server.domain.route.exception.RouteSuccessCode;
import com.ridingmate.api_server.domain.route.service.GpxRecommendationService;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.repository.UserRepository;
import com.ridingmate.api_server.global.exception.CommonResponse;
import com.ridingmate.api_server.global.service.GpsDataEncryptionService;
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

import java.util.Map;

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
    private final GpsDataEncryptionService gpsDataEncryptionService;
    private final LocationDataAccessLogService locationDataAccessLogService;

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
        User user = userRepository.findById(9999L)
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
                    description = "썸네일 이미지 파일 (선택사항)", 
                    required = false
            )
            @RequestParam(value = "thumbnailImage", required = false) MultipartFile thumbnailImage,
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
        User user = userRepository.findById(9999L)
                .orElseThrow(() -> new RuntimeException("ID 9999번 사용자를 찾을 수 없습니다. 먼저 사용자를 생성해주세요."));

        // 파일 유효성 검사
        validateGpxFile(gpxFile);

        // GPX 파일로부터 추천코스 생성
        GpxUploadResponse response = gpxRecommendationService.createRecommendationFromGpx(
                user, gpxFile, thumbnailImage, title, description,
                difficulty, region, landscapeType, recommendationType
        );

        log.info("테스트 GPX 파일 업로드 완료: routeId={}", response.routeId());

        return ResponseEntity
                .status(RouteSuccessCode.GPX_UPLOAD_SUCCESS.getStatus())
                .body(CommonResponse.success(RouteSuccessCode.GPX_UPLOAD_SUCCESS, response));
    }

    @Operation(
            summary = "기존 GPS 데이터 암호화 (마이그레이션용)",
            description = """
            기존 DOUBLE 타입으로 저장된 GPS 데이터를 암호화합니다.
            
            **주의사항:**
            - 이 작업은 한 번만 실행해야 합니다!
            - 대량의 데이터가 있을 경우 시간이 오래 걸릴 수 있습니다.
            - ActivityGpsLog와 RouteGpsLog의 lat/lng/elevation을 암호화합니다.
            - 배치 단위로 처리되므로 중간에 실패해도 일부는 암호화됩니다.
            
            **실행 전 확인:**
            1. DB 백업 완료 여부
            2. GPS_ENCRYPTION_KEY 환경 변수 설정 여부
            3. 컬럼 타입이 TEXT로 변경되었는지 (Flyway 마이그레이션)
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공: 암호화 완료"),
            @ApiResponse(responseCode = "500", description = "실패: 암호화 중 오류 발생")
    })
    @PostMapping("/encrypt-gps-data")
    public ResponseEntity<String> encryptGpsData() {
        log.warn("🔐 [GPS 암호화] 기존 GPS 데이터 암호화 시작 - 이 작업은 한 번만 실행해야 합니다!");
        
        try {
            gpsDataEncryptionService.encryptAllGpsData();
            log.info("✅ [GPS 암호화] 기존 GPS 데이터 암호화 완료");
            return ResponseEntity.ok("✅ GPS 데이터 암호화가 완료되었습니다.");
            
        } catch (Exception e) {
            log.error("❌ [GPS 암호화] 기존 GPS 데이터 암호화 실패", e);
            return ResponseEntity.status(500).body("❌ 암호화 실패: " + e.getMessage());
        }
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

    /**
     * 기존 Route/Activity에 대한 위치정보 수집 로그 소급 생성
     * (테스트/마이그레이션용)
     */
    @PostMapping("/test/backfill-location-logs")
    public ResponseEntity<Map<String, Object>> backfillLocationAccessLogs() {
        try {
            Map<String, Object> result = locationDataAccessLogService.backfillCollectionLogs();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("위치정보 수집 로그 소급 생성 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage(),
                    "message", "위치정보 수집 로그 소급 생성 실패"
            ));
        }
    }

} 