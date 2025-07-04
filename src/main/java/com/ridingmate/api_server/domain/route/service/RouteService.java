package com.ridingmate.api_server.domain.route.service;

import com.ridingmate.api_server.domain.route.dto.request.CreateRouteRequest;
import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.route.exception.RouteErrorCode;
import com.ridingmate.api_server.domain.route.exception.RouteException;
import com.ridingmate.api_server.domain.route.repository.RouteRepository;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.repository.UserRepository;
import com.ridingmate.api_server.global.config.AppConfigProperties;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final AppConfigProperties appConfigProperties;

    private final RouteRepository routeRepository;
    private final UserRepository userRepository;

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
                .build();

        routeRepository.save(route);

        route.updateThumbnailImagePath(createThumbnailImagePath(route.getId()));

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

    private String getShareId(Route route){
        String shareId = route.getShareId();
        if ( shareId == null || shareId.isBlank()) {
            throw new RouteException(RouteErrorCode.SHARE_ID_NOT_FOUND);
        }
        return shareId;
    }
}
