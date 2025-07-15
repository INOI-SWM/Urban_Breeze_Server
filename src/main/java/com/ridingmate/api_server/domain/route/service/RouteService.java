package com.ridingmate.api_server.domain.route.service;

import com.ridingmate.api_server.domain.route.dto.request.CreateRouteRequest;
import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.route.entity.UserRoute;
import com.ridingmate.api_server.domain.route.enums.RouteRelationType;
import com.ridingmate.api_server.domain.route.enums.RouteSortType;
import com.ridingmate.api_server.domain.route.exception.RouteErrorCode;
import com.ridingmate.api_server.domain.route.exception.RouteException;
import com.ridingmate.api_server.domain.route.repository.RouteRepository;
import com.ridingmate.api_server.domain.route.repository.UserRouteRepository;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.repository.UserRepository;
import com.ridingmate.api_server.global.config.AppConfigProperties;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.LineString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final AppConfigProperties appConfigProperties;

    private final RouteRepository routeRepository;
    private final UserRouteRepository userRouteRepository;
    private final UserRepository userRepository;

    @Transactional
    public Route createRoute(CreateRouteRequest request, LineString routeLine) {
        double averageGradient = calculateAverageGradient(request.elevationGain(), request.distance());

        User mockUser = userRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Mock user not found"));

        Route route = Route.builder()
                .user(mockUser)
                .shareId(UUID.randomUUID().toString())
                .title(request.title())
                .routeLine(routeLine)
                .totalDistance(request.distance())
                .totalDuration(Duration.ofMinutes(request.duration()))
                .totalElevationGain(request.elevationGain())
                .averageGradient(averageGradient)
                .minLon(request.bbox().get(0))
                .minLat(request.bbox().get(1))
                .maxLon(request.bbox().get(2))
                .maxLat(request.bbox().get(3))
                .build();

        routeRepository.save(route);

        route.updateThumbnailImagePath(createThumbnailImagePath(route.getId()));

        // 생성자와 경로 간의 OWNER 관계 생성
        createUserRouteRelation(mockUser, route, RouteRelationType.OWNER);

        return route;
    }

    private double calculateAverageGradient(double totalElevationGain, double totalDistance) {
        if (totalDistance == 0) return 0.0;
        return (totalElevationGain / totalDistance) * 100;
    }

    private String createThumbnailImagePath(Long routeId) {
        String uuid = UUID.randomUUID().toString();
        return String.format("ridingmate/route-thumbnails/%d/%s.png", routeId, uuid);
    }

    public String createShareLink(Long routeId) {
        Route route = getRoute(routeId);
        String shareId = getShareId(route);
        String scheme = appConfigProperties.scheme();

        return String.format("%s/%s", scheme, shareId);
    }

    public Route getRoute(Long routeId) {
        return routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteException(RouteErrorCode.ROUTE_NOT_FOUND));
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

    private String getShareId(Route route){
        String shareId = route.getShareId();
        if ( shareId == null || shareId.isBlank()) {
            throw new RouteException(RouteErrorCode.SHARE_ID_NOT_FOUND);
        }
        return shareId;
    }

    /**
     * 사용자별 경로 목록을 정렬 타입과 필터에 따라 조회
     * @param userId 사용자 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sortType 정렬 타입
     * @param relationTypes 관계 타입 필터 (null이면 모든 관계 타입)
     * @return 정렬된 경로 페이지
     */
    public Page<Route> getRoutesByUser(Long userId, int page, int size, RouteSortType sortType, List<RouteRelationType> relationTypes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Pageable pageable = PageRequest.of(page, size, sortType.getSort());

        if (relationTypes == null || relationTypes.isEmpty()) {
            // 모든 관계 타입 조회
            return routeRepository.findByUserWithRelations(user, pageable);
        } else if (relationTypes.size() == 1) {
            // 단일 관계 타입 조회
            return routeRepository.findByUserAndRelationType(user, relationTypes.get(0), pageable);
        } else {
            // 여러 관계 타입 조회
            return routeRepository.findByUserAndRelationTypes(user, relationTypes, pageable);
        }
    }
}
