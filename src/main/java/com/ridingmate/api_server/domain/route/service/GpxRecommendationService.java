package com.ridingmate.api_server.domain.route.service;

import com.ridingmate.api_server.domain.route.dto.response.GpxUploadResponse;
import com.ridingmate.api_server.domain.route.entity.Recommendation;
import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.route.entity.RouteGpsLog;
import com.ridingmate.api_server.domain.route.entity.UserRoute;
import com.ridingmate.api_server.domain.route.enums.*;
import com.ridingmate.api_server.domain.route.repository.RecommendationRepository;
import com.ridingmate.api_server.domain.route.repository.RouteGpsLogRepository;
import com.ridingmate.api_server.domain.route.repository.RouteRepository;
import com.ridingmate.api_server.domain.route.repository.UserRouteRepository;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.global.util.GeometryUtil;
import com.ridingmate.api_server.global.util.GpxGenerator;
import com.ridingmate.api_server.infra.aws.s3.S3Manager;
import com.ridingmate.api_server.infra.geoapify.GeoapifyClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GpxRecommendationService {

    private final GpxParserService gpxParserService;
    private final RouteRepository routeRepository;
    private final RouteGpsLogRepository routeGpsLogRepository;
    private final RecommendationRepository recommendationRepository;
    private final UserRouteRepository userRouteRepository;
    private final S3Manager s3Manager;
    private final GeoapifyClient geoapifyClient;

    @Transactional
    public GpxUploadResponse createRecommendationFromGpx(
            User user, MultipartFile gpxFile, MultipartFile thumbnailImage, String title, String description,
            com.ridingmate.api_server.domain.route.enums.Difficulty difficulty,
            com.ridingmate.api_server.domain.route.enums.Region region,
            com.ridingmate.api_server.domain.route.enums.LandscapeType landscapeType,
            RecommendationType recommendationType) {
        try {
            // 1. GPX 파일 파싱
            GpxParserService.GpxParseResult parseResult = gpxParserService.parseGpxFile(gpxFile);

            // 2. Route 엔티티 생성 및 저장
            Route route = createRoute(user, title, description, difficulty, region, landscapeType, parseResult);
            
            // 3. RouteGpsLog 엔티티들 생성 및 저장
            createRouteGpsLogs(route, parseResult.coordinates());

            // 4. Recommendation 엔티티 생성 및 저장
            Recommendation recommendation = createRecommendation(route, recommendationType);

            // 5. UserRoute 관계 생성 (OWNER 및 RECOMMENDED)
            createUserRouteRelation(user, route, RouteRelationType.OWNER);
            createUserRouteRelation(user, route, RouteRelationType.RECOMMENDED);

            // 6. GPX 파일 S3 업로드
            String gpxFilePath = uploadGpxFileToS3(route.getId(), gpxFile);
            route.updateGpxFilePath(gpxFilePath);
            routeRepository.save(route);

            // 7. 썸네일 이미지 처리
            String thumbnailUrl = handleThumbnailImage(route, thumbnailImage, parseResult.coordinates());

            return GpxUploadResponse.from(route, recommendation, thumbnailUrl, s3Manager.getPresignedUrl(gpxFilePath));

        } catch (IOException e) {
            log.error("GPX 파일 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("GPX 파일 처리 중 오류가 발생했습니다.", e);
        }
    }

    private Route createRoute(User user, String title, String description,
                            Difficulty difficulty,
                            Region region,
                            LandscapeType landscapeType,
                            GpxParserService.GpxParseResult parseResult) {
        Route route = Route.builder()
                .user(user)
                .title(title)
                .description(description)
                .distance(parseResult.totalDistance())
                .duration(parseResult.duration())
                .elevationGain(parseResult.elevationGain())
                .landscapeType(landscapeType)
                .region(region)
                .difficulty(difficulty)
                .routeLine(parseResult.routeLine())
                .maxLat(parseResult.maxLat())
                .maxLon(parseResult.maxLon())
                .minLat(parseResult.minLat())
                .minLon(parseResult.minLon())
                .build();

        return routeRepository.save(route);
    }

    private void createRouteGpsLogs(Route route, List<Coordinate> coordinates) {
        LocalDateTime baseTime = LocalDateTime.now();
        List<RouteGpsLog> routeGpsLogs = new ArrayList<>();

        for (int i = 0; i < coordinates.size(); i++) {
            Coordinate coord = coordinates.get(i);
            LocalDateTime logTime = baseTime.plusSeconds(i);

            RouteGpsLog routeGpsLog = RouteGpsLog.builder()
                    .route(route)
                    .longitude(coord.getX())
                    .latitude(coord.getY())
                    .elevation(coord.getZ())
                    .logTime(logTime)
                    .build();
            routeGpsLogs.add(routeGpsLog);
        }
        routeGpsLogRepository.saveAll(routeGpsLogs);
    }

    private Recommendation createRecommendation(Route route, RecommendationType recommendationType) {
        Recommendation recommendation = Recommendation.builder()
                .route(route)
                .recommendationType(recommendationType)
                .build();
        return recommendationRepository.save(recommendation);
    }

    private void createUserRouteRelation(User user, Route route, RouteRelationType relationType) {
        UserRoute userRoute = UserRoute.builder()
                .user(user)
                .route(route)
                .relationType(relationType)
                .build();
        userRouteRepository.save(userRoute);
    }

    private String uploadGpxFileToS3(Long routeId, MultipartFile gpxFile) throws IOException {
        String gpxFilePath = GpxGenerator.generateGpxFilePath(routeId);
        s3Manager.uploadByteFiles(gpxFilePath, gpxFile.getInputStream().readAllBytes(),  gpxFile.getContentType());
        return gpxFilePath;
    }

    /**
     * 썸네일 이미지 처리 (사용자 업로드 또는 자동 생성)
     */
    private String handleThumbnailImage(Route route, MultipartFile thumbnailImage, List<Coordinate> coordinates) {
        try {
            // 1. 사용자가 썸네일 이미지를 업로드한 경우
            if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
                return uploadUserThumbnail(route, thumbnailImage);
            }
            
            // 2. 사용자 썸네일이 없는 경우 자동 생성
            return generateAutoThumbnail(route, coordinates);
            
        } catch (Exception e) {
            log.error("썸네일 처리 실패: routeId={}, error={}", 
                    route.getId(), e.getMessage(), e);
            return "https://via.placeholder.com/300x200?text=Thumbnail+Error";
        }
    }

    /**
     * 사용자 업로드 썸네일 처리
     */
    private String uploadUserThumbnail(Route route, MultipartFile thumbnailImage) throws IOException {
        // 이미지 유효성 검사
        validateImageFile(thumbnailImage);
        
        // 썸네일 경로 생성 및 S3 업로드
        String thumbnailPath = createThumbnailImagePath(route.getId());
        s3Manager.uploadByteFiles(thumbnailPath, thumbnailImage.getBytes(), thumbnailImage.getContentType());
        
        // Route 엔티티에 썸네일 경로 업데이트
        route.updateThumbnailImagePath(thumbnailPath);
        routeRepository.save(route);
        
        log.info("사용자 썸네일 업로드 성공: routeId={}, path={}, fileName={}", 
                route.getId(), thumbnailPath, thumbnailImage.getOriginalFilename());

        return s3Manager.getPresignedUrl(thumbnailPath);
    }

    /**
     * 자동 썸네일 생성 (사용자 썸네일이 없는 경우)
     */
    private String generateAutoThumbnail(Route route, List<Coordinate> coordinates) {
        try {
            if (coordinates.size() < 2) {
                log.warn("자동 썸네일 생성 건너뜀: routeId={}, 좌표 부족 (count={})", 
                        route.getId(), coordinates.size());
                return "https://via.placeholder.com/300x200?text=No+Route+Data";
            }

            // 2D 좌표로 LineString 생성
            Coordinate[] coords2D = coordinates.stream()
                    .map(coord -> new Coordinate(coord.getX(), coord.getY()))
                    .toArray(Coordinate[]::new);
            
            LineString routeLine = GeometryUtil.createLineStringFromCoordinates(coords2D);

            // Geoapify를 통해 썸네일 생성
            byte[] thumbnailBytes = geoapifyClient.getStaticMap(routeLine);

            // 썸네일 경로 생성 및 S3 업로드
            String thumbnailPath = createThumbnailImagePath(route.getId());
            s3Manager.uploadByteFiles(thumbnailPath, thumbnailBytes, "image/png");
            
            // Route 엔티티에 썸네일 경로 업데이트
            route.updateThumbnailImagePath(thumbnailPath);
            routeRepository.save(route);
            
            log.info("자동 썸네일 생성 성공: routeId={}, path={}, coordCount={}", 
                    route.getId(), thumbnailPath, coordinates.size());

            return s3Manager.getPresignedUrl(thumbnailPath);

        } catch (Exception e) {
            log.error("자동 썸네일 생성 실패: routeId={}, error={}", 
                    route.getId(), e.getMessage(), e);
            return "https://via.placeholder.com/300x200?text=Auto+Thumbnail+Error";
        }
    }

    /**
     * 이미지 파일 유효성 검사
     */
    private void validateImageFile(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 선택되지 않았습니다.");
        }

        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        // 파일 크기 제한 (5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (imageFile.getSize() > maxSize) {
            throw new IllegalArgumentException("이미지 파일 크기는 5MB를 초과할 수 없습니다.");
        }
    }

    /**
     * 썸네일 이미지 경로 생성
     */
    private String createThumbnailImagePath(Long routeId) {
        return String.format("thumbnails/routes/route_%d_%s.png", 
                routeId, System.currentTimeMillis());
    }
}
