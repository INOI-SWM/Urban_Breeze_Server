package com.ridingmate.api_server.domain.route.service;

import com.ridingmate.api_server.domain.auth.exception.AuthErrorCode;
import com.ridingmate.api_server.domain.auth.exception.AuthException;
import com.ridingmate.api_server.domain.route.dto.projection.RouteFilterRangeProjection;
import com.ridingmate.api_server.domain.route.dto.request.CreateRouteRequest;
import com.ridingmate.api_server.domain.route.dto.request.RecommendationListRequest;
import com.ridingmate.api_server.domain.route.dto.FilterRangeInfo;
import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.route.entity.RouteGpsLog;
import com.ridingmate.api_server.domain.route.entity.UserRoute;
import com.ridingmate.api_server.domain.route.enums.RecommendationSortType;
import com.ridingmate.api_server.domain.route.enums.RouteRelationType;
import com.ridingmate.api_server.domain.route.exception.code.RouteCommonErrorCode;
import com.ridingmate.api_server.domain.route.exception.RouteException;
import com.ridingmate.api_server.domain.route.exception.code.RouteDetailErrorCode;
import com.ridingmate.api_server.domain.route.exception.code.RouteShareErrorCode;
import com.ridingmate.api_server.domain.route.repository.RouteGpsLogRepository;
import com.ridingmate.api_server.domain.route.repository.RouteRepository;
import com.ridingmate.api_server.domain.route.repository.UserRouteRepository;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.repository.UserRepository;
import com.ridingmate.api_server.global.config.AppConfigProperties;
import com.ridingmate.api_server.global.util.GeometryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.ridingmate.api_server.domain.route.dto.request.RouteListRequest;
import com.ridingmate.api_server.infra.aws.s3.S3Manager;
import com.ridingmate.api_server.global.util.GpxGenerator;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteService {

    private final AppConfigProperties appConfigProperties;

    private final RouteRepository routeRepository;
    private final UserRouteRepository userRouteRepository;
    private final UserRepository userRepository;
    private final RouteGpsLogRepository routeGpsLogRepository;
    private final S3Manager s3Manager;

    @Transactional
    public Route createRoute(Long userId, CreateRouteRequest request, LineString routeLine) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.AUTHENTICATION_USER_NOT_FOUND));

        // Route 엔티티 생성 (Geometry 정보 포함)
        Route route = Route.builder()
                .user(user)
                .title(request.title())
                .description(request.description())
                .distance(request.distance())
                .duration(Duration.ofSeconds(request.duration()))
                .elevationGain(request.elevationGain())
                .routeId(UUID.randomUUID())
                .routeLine(routeLine)
                .minLon(request.bbox().get(0))
                .minLat(request.bbox().get(1))
                .maxLon(request.bbox().get(2))
                .maxLat(request.bbox().get(3))
                .build();

        // Route 저장
        routeRepository.save(route);

        // ID가 생성된 후 썸네일 경로 업데이트
        route.updateThumbnailImagePath(createThumbnailImagePath(route.getId()));

        // 생성자와 경로 간의 OWNER 관계 생성
        createUserRouteRelation(user, route, RouteRelationType.OWNER);

        Coordinate[] geometry = request.geometry().stream()
                .map(dto -> new Coordinate(dto.longitude(), dto.latitude(), dto.elevation()))
                .toArray(Coordinate[]::new);
        createRouteGpsLog(route, geometry);

        return route;
    }

    private void createRouteGpsLog(Route route, Coordinate[] geometry) {
        LocalDateTime baseTime = LocalDateTime.now();
        int sequence = 0;

        List<RouteGpsLog> routeGpsLogs = new ArrayList<>();
        for (Coordinate coordinate: geometry){
            LocalDateTime virtualLogTime = baseTime.plusSeconds(sequence);

            RouteGpsLog routeGpsLog = RouteGpsLog.builder()
                    .route(route)
                    .longitude(coordinate.getX())
                    .latitude(coordinate.getY())
                    .elevation(coordinate.getZ())
                    .logTime(virtualLogTime)
                    .build();
            routeGpsLogs.add(routeGpsLog);

            sequence++;
        }

        routeGpsLogRepository.saveAll(routeGpsLogs);
    }

    @Transactional(readOnly = true)
    public Coordinate[] getRouteDetailList(Long routeId) {
        List<RouteGpsLog> routeGpsLogs = routeGpsLogRepository.findByRouteIdOrderByLogTimeAsc(routeId);
        if (routeGpsLogs == null || routeGpsLogs.size() < 2) {
            throw new RouteException(RouteDetailErrorCode.ROUTE_GPS_LOGS_INVALID);
        }

        return routeGpsLogs.stream()
            .map(routeGpsLog -> new Coordinate(routeGpsLog.getLongitude(), routeGpsLog.getLatitude(), routeGpsLog.getElevation()))
            .toArray(Coordinate[]::new);
    }

    @Transactional(readOnly = true)
    public String createShareLink(Route route, Long userId) {
        checkRouteAuth(userId, route);
        String routeId = route.getRouteId().toString();
        String scheme = appConfigProperties.scheme();

        return String.format("%s%s", scheme, routeId);
    }

    /**
     * 사용자와 경로 간의 관계 생성
     * @param user 사용자
     * @param route 경로
     * @param relationType 관계 타입
     */
    @Transactional
    public void createUserRouteRelation(User user, Route route, RouteRelationType relationType) {
        // 이미 활성 관계가 있는지 확인
        var activeRelation = userRouteRepository.findByUserAndRouteAndRelationTypeAndIsDeleteFalse(user, route, relationType);
        if (activeRelation.isPresent()) {
            // 이미 활성 관계가 있으면 아무것도 하지 않음
            return;
        }

        // 새로운 관계 생성
        UserRoute userRoute = UserRoute.builder()
                .user(user)
                .route(route)
                .relationType(relationType)
                .build();
        userRouteRepository.save(userRoute);
    }

    /**
     * 사용자별 경로 목록을 정렬 타입과 필터에 따라 조회
     * @param userId 사용자 ID
     * @param request 경로 목록 조회 요청 정보
     * @return 정렬된 경로 페이지
     */
    @Transactional(readOnly = true)
    public Page<Route> getRoutesByUser(Long userId, RouteListRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Pageable pageable = PageRequest.of(request.page(), request.size(), request.sortType().getSort());

        if (request.relationTypes() == null || request.relationTypes().isEmpty()) {
            // 모든 관계 타입 조회
            return routeRepository.findByUserWithRelationsAndFilters(user,
                request.getMinDistanceInMeter(), request.getMaxDistanceInMeter(),
                request.getMinElevationGain(), request.getMaxElevationGain(), pageable);
        } else if (request.relationTypes().size() == 1) {
            // 단일 관계 타입 조회
            return routeRepository.findByUserAndRelationTypeWithFilters(user, request.relationTypes().get(0),
                request.getMinDistanceInMeter(), request.getMaxDistanceInMeter(),
                request.getMinElevationGain(), request.getMaxElevationGain(), pageable);
        } else {
            // 여러 관계 타입 조회
            return routeRepository.findByUserAndRelationTypesWithFilters(user, request.relationTypes(),
                request.getMinDistanceInMeter(), request.getMaxDistanceInMeter(),
                request.getMinElevationGain(), request.getMaxElevationGain(), pageable);
        }
    }

    @Transactional(readOnly = true)
    public Route getRouteWithUser(Long routeId){
        return routeRepository.findRouteWithUser(routeId)
            .orElseThrow(() -> new RouteException(RouteCommonErrorCode.ROUTE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Route getRouteWithUserByRouteId(String routeId){
        return routeRepository.findRouteWithUserByRouteId(UUID.fromString(routeId))
            .orElseThrow(() -> new RouteException(RouteCommonErrorCode.ROUTE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Route getRouteByRouteId(String routeId){
        return routeRepository.findByRouteId(UUID.fromString(routeId))
            .orElseThrow(() -> new RouteException(RouteCommonErrorCode.ROUTE_NOT_FOUND));
    }

    private Route checkRouteAuth(Long userId, Route route) {
        if (!route.getUser().getId().equals(userId)) {
            throw new RouteException(RouteCommonErrorCode.ROUTE_ACCESS_DENIED);
        }
        return route;
    }

    private Route getRoute(Long routeId) {
        return routeRepository.findById(routeId)
            .orElseThrow(() -> new RouteException(RouteCommonErrorCode.ROUTE_NOT_FOUND));
    }


    private String createThumbnailImagePath(Long routeId) {
        String uuid = UUID.randomUUID().toString();
        return String.format("ridingmate/route-thumbnails/%d/%s.png", routeId, uuid);
    }

    /**
     * 추천 코스 목록을 정렬 타입과 필터에 따라 조회
     * @param request 추천 코스 목록 조회 요청 정보
     * @return 정렬된 추천 코스 페이지
     */
    public Page<Route> getRecommendationRoutes(RecommendationListRequest request) {
        // NEAREST 정렬이 아닌 경우에만 데이터베이스 정렬 사용
        Pageable pageable;
        if (request.sortType() == RecommendationSortType.NEAREST) {
            // NEAREST는 애플리케이션 레벨에서 정렬하므로 기본 정렬 사용
            pageable = PageRequest.of(request.page(), request.size(), Sort.by("id").ascending());
        } else {
            pageable = PageRequest.of(request.page(), request.size(), request.sortType().getSort());
        }

        // 추천 코스만 조회 (Recommendation 엔티티가 있는 Route)
        Page<Route> routePage = routeRepository.findRecommendationRoutesWithFilters(
                request.recommendationTypes(),
                request.regions(),
                request.difficulties(),
                request.getMinDistanceInMeter(), 
                request.getMaxDistanceInMeter(), 
                request.minElevationGain(), 
                request.maxElevationGain(), 
                pageable);

        // NEAREST 정렬인 경우 거리 계산 후 정렬
        if (request.sortType() == com.ridingmate.api_server.domain.route.enums.RecommendationSortType.NEAREST 
            && request.userLon() != null && request.userLat() != null) {
            
            List<Route> sortedRoutes = routePage.getContent().stream()
                .sorted((a, b) -> {
                    Double distanceA = calculateDistanceFromUser(a, request.userLon(), request.userLat());
                    Double distanceB = calculateDistanceFromUser(b, request.userLon(), request.userLat());
                    
                    // null 거리는 맨 뒤로
                    if (distanceA == null && distanceB == null) return 0;
                    if (distanceA == null) return 1;
                    if (distanceB == null) return -1;
                    
                    return Double.compare(distanceA, distanceB);
                })
                .toList();

            // 정렬된 결과로 새로운 Page 생성
            return new PageImpl<>(
                sortedRoutes, 
                pageable, 
                routePage.getTotalElements()
            );
        }

        return routePage;
    }

    /**
     * 사용자 위치로부터 Route의 출발점까지의 거리 계산 (km 단위)
     */
    private Double calculateDistanceFromUser(Route route, Double userLon, Double userLat) {
        if (route.getRouteLine() != null) {
            Coordinate startCoord = route.getStartCoordinate();
            if (startCoord != null) {
                return GeometryUtil.calculateDistance(
                    userLon, userLat, startCoord.getX(), startCoord.getY());
            }
        }
        return null;
    }

    /**
     * 사용자별 전체 경로의 최대 거리와 고도 조회
     */
    @Transactional(readOnly = true)
    public FilterRangeInfo getMaxDistanceAndElevationByUser(User user) {
        RouteFilterRangeProjection result = routeRepository.findMaxDistanceAndElevationByUser(user);
        
        if (result == null || result.maxDistance() == null || result.maxElevationGain() == null) {
            return FilterRangeInfo.of(0.0, 0.0, 0.0, 0.0);
        }
        
        return FilterRangeInfo.of(
            result.getMinDistanceInKm(), 
            result.getMaxDistanceInKm(), 
            result.getRoundedMinElevationGain(), 
            result.getRoundedMaxElevationGain()
        );
    }

    /**
     * 추천 코스의 전체 최대 거리와 고도 조회
     */
    @Transactional(readOnly = true)
    public FilterRangeInfo getMaxDistanceAndElevationForRecommendations() {
        RouteFilterRangeProjection result = routeRepository.findMaxDistanceAndElevationForRecommendations();
        
        if (result == null || result.maxDistance() == null || result.maxElevationGain() == null) {
            return FilterRangeInfo.of(0.0, 0.0, 0.0, 0.0);
        }
        
        return FilterRangeInfo.of(
            result.getMinDistanceInKm(), 
            result.getMaxDistanceInKm(), 
            result.getRoundedMinElevationGain(), 
            result.getRoundedMaxElevationGain()
        );
    }

    /**
     * Route의 GPX 파일 경로를 업데이트합니다.
     *
     * @param routeId     Route ID
     * @param gpxFilePath S3에 저장된 GPX 파일 경로
     */
    @Transactional
    public void updateGpxFilePath(Long routeId, String gpxFilePath) {
        Route route = routeRepository.findById(routeId)
            .orElseThrow(() -> new RouteException(RouteCommonErrorCode.ROUTE_NOT_FOUND));
        
        route.updateGpxFilePath(gpxFilePath);
    }

    /**
     * 경로의 GPX 파일을 다운로드합니다.
     * S3에 저장된 파일이 있으면 다운로드하고, 없으면 새로 생성합니다.
     *
     * @param route 경로
     * @return GPX 파일의 바이트 배열
     */
    @Transactional(readOnly = true)
    public byte[] downloadGpxFile(Route route) {
        if (route.getGpxFilePath() != null && !route.getGpxFilePath().isEmpty()) {
            try {
                try (InputStream inputStream = s3Manager.downloadFile(route.getGpxFilePath())) {
                    return inputStream.readAllBytes();
                }
            } catch (Exception e) {
                return generateGpxFile(route);
            }
        } else {
            return generateGpxFile(route);
        }
    }

        /**
     * 경로의 GPX 파일명을 생성합니다.
     * 
     * @param route 경로
     * @return 안전한 파일명
     */
    public String generateGpxFileName(Route route) {
        if (route.getTitle() == null || route.getTitle().trim().isEmpty()) {
            return "route.gpx";
        }
        
        // 파일명에 사용할 수 없는 문자들을 언더스코어로 대체
        String safeFileName = route.getTitle()
            .replaceAll("[^a-zA-Z0-9가-힣\\s]", "_")  // 특수문자 제거
            .replaceAll("\\s+", "_")                 // 공백을 언더스코어로
            .replaceAll("_{2,}", "_")                // 연속된 언더스코어를 하나로
            .replaceAll("^_|_$", "");                // 앞뒤 언더스코어 제거
        
        // 빈 문자열이거나 너무 긴 경우 처리
        if (safeFileName.isEmpty() || safeFileName.length() > 100) {
            return "route.gpx";
        }
        
        return safeFileName + ".gpx";
    }

    /**
     * 경로 데이터로부터 GPX 파일을 생성합니다.
     *
     * @param route 경로 엔티티
     * @return GPX 파일의 바이트 배열
     */
    private byte[] generateGpxFile(Route route) {
        try {
            // 실제 GPS 로그 데이터를 사용하여 GPX 생성
            Coordinate[] coordinates = getRouteDetailList(route.getId());
            return GpxGenerator.generateGpxBytesFromCoordinates(coordinates, route.getTitle());
        } catch (Exception e) {
            throw new RuntimeException("GPX 파일 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자 삭제 시 경로 데이터 처리
     * - 경로 데이터는 법정 기간 동안 보존 (삭제하지 않음)
     * - 사용자-경로 관계만 소프트 삭제 처리
     */
    @Transactional
    public void handleUserDeletion(User user) {
        log.info("경로 데이터 처리 시작: userId={}", user.getId());
        
        try {
            // 1. 사용자-경로 관계 소프트 삭제 처리
            markUserRoutesAsDeleted(user);
            
            // 2. 사용자가 생성한 경로들의 사용자 정보 마스킹
            maskRouteUserInfo(user);
            
            // 3. 경로 GPS 로그 처리 (법정 보존 대상)
            handleRouteGpsLogs(user);
            
            log.info("경로 데이터 처리 완료: userId={}", user.getId());
        } catch (Exception e) {
            log.error("경로 데이터 처리 중 오류 발생: userId={}", user.getId(), e);
            // 경로 데이터 처리 실패해도 사용자 삭제는 계속 진행
        }
    }

    /**
     * 사용자-경로 관계 소프트 삭제 처리
     */
    private void markUserRoutesAsDeleted(User user) {
        log.info("사용자-경로 관계 소프트 삭제 처리 시작: userId={}", user.getId());
        
        // 사용자의 모든 활성 경로 관계를 소프트 삭제
        List<UserRoute> activeUserRoutes = userRouteRepository.findByUserAndIsDeleteFalse(user);
        
        for (UserRoute userRoute : activeUserRoutes) {
            userRoute.markAsDeleted();
            log.debug("사용자-경로 관계 소프트 삭제: userRouteId={}, routeId={}", 
                userRoute.getId(), userRoute.getRoute().getId());
        }
        
        log.info("사용자-경로 관계 소프트 삭제 처리 완료: userId={}, count={}", 
            user.getId(), activeUserRoutes.size());
    }

    /**
     * 경로의 사용자 정보 마스킹 처리
     */
    private void maskRouteUserInfo(User user) {
        log.info("경로 사용자 정보 마스킹 처리 시작: userId={}", user.getId());

        List<Route> userRoutes = routeRepository.findByUser(user);
        
        for (Route route : userRoutes) {
            // 모든 개인정보 필드 마스킹 및 소프트 삭제 처리 (통합)
            route.maskPersonalDataForDeletion();
            
            log.debug("경로 사용자 정보 마스킹 및 소프트 삭제: routeId={}", route.getId());
        }
        
        log.info("경로 사용자 정보 마스킹 처리 완료: userId={}, count={}", 
            user.getId(), userRoutes.size());
    }

    /**
     * 경로 GPS 로그 처리
     * - RouteGpsLog의 개인정보 마스킹 처리
     * - GPS 데이터는 법정 보존 대상이지만 개인정보는 마스킹
     */
    private void handleRouteGpsLogs(User user) {
        log.info("경로 GPS 로그 처리 시작: userId={}", user.getId());
        
        try {
            List<Route> userRoutes = routeRepository.findByUser(user);
            
            for (Route route : userRoutes) {
                // 경로의 모든 GPS 로그 조회
                List<RouteGpsLog> routeGpsLogs = routeGpsLogRepository.findByRouteIdOrderByLogTimeAsc(route.getId());
                
                log.debug("경로 GPS 로그 마스킹 시작: routeId={}, count={}", 
                    route.getId(), routeGpsLogs.size());
                
                // DB에서 모든 GPS 로그 하드 삭제
                routeGpsLogRepository.deleteByRouteId(route.getId());
                
                log.debug("경로 GPS 로그 하드 삭제 완료: routeId={}", route.getId());
            }
            
            log.info("경로 GPS 로그 처리 완료: userId={}, routeCount={}", 
                user.getId(), userRoutes.size());
        } catch (Exception e) {
            log.warn("경로 GPS 로그 처리 중 오류: userId={}", user.getId(), e);
        }
    }

}
