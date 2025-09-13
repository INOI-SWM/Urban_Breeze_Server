package com.ridingmate.api_server.domain.route.service;

import com.ridingmate.api_server.domain.auth.exception.AuthErrorCode;
import com.ridingmate.api_server.domain.auth.exception.AuthException;
import com.ridingmate.api_server.domain.route.dto.request.CreateRouteRequest;
import com.ridingmate.api_server.domain.route.dto.request.RecommendationListRequest;
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

@Service
@RequiredArgsConstructor
public class RouteService {

    private final AppConfigProperties appConfigProperties;

    private final RouteRepository routeRepository;
    private final UserRouteRepository userRouteRepository;
    private final UserRepository userRepository;
    private final RouteGpsLogRepository routeGpsLogRepository;

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
                .shareId(UUID.randomUUID().toString())
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
    public String createShareLink(Long routeId, Long userId) {
        Route route = getUserRoute(userId, routeId);
        String shareId = getShareId(route);
        String scheme = appConfigProperties.scheme();

        return String.format("%s/%s", scheme, shareId);
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
                request.minElevationGain(), request.maxElevationGain(), pageable);
        } else if (request.relationTypes().size() == 1) {
            // 단일 관계 타입 조회
            return routeRepository.findByUserAndRelationTypeWithFilters(user, request.relationTypes().get(0),
                request.getMinDistanceInMeter(), request.getMaxDistanceInMeter(),
                request.minElevationGain(), request.maxElevationGain(), pageable);
        } else {
            // 여러 관계 타입 조회
            return routeRepository.findByUserAndRelationTypesWithFilters(user, request.relationTypes(),
                request.getMinDistanceInMeter(), request.getMaxDistanceInMeter(),
                request.minElevationGain(), request.maxElevationGain(), pageable);
        }
    }

    @Transactional(readOnly = true)
    public Route getRouteWithUser(Long routeId){
        return routeRepository.findRouteWithUser(routeId)
            .orElseThrow(() -> new RouteException(RouteCommonErrorCode.ROUTE_NOT_FOUND));
    }

    private Route getUserRoute(Long userId, Long routeId) {
        Route route = getRoute(routeId);
        if (!route.getUser().getId().equals(userId)) {
            throw new RouteException(RouteCommonErrorCode.ROUTE_ACCESS_DENIED);
        }
        return route;
    }

    private Route getRoute(Long routeId) {
        return routeRepository.findById(routeId)
            .orElseThrow(() -> new RouteException(RouteCommonErrorCode.ROUTE_NOT_FOUND));
    }

    private String getShareId(Route route){
        String shareId = route.getShareId();
        if ( shareId == null || shareId.isBlank()) {
            throw new RouteException(RouteShareErrorCode.SHARE_ID_NOT_FOUND);
        }
        return shareId;
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

}
