package com.ridingmate.api_server.domain.route.service;

import com.ridingmate.api_server.domain.route.dto.request.CreateRouteRequest;
import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.route.repository.RouteRepository;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final UserRepository userRepository;

    public Route createRoute(CreateRouteRequest request, LineString routeLine) {
        double averageGradient = calculateAverageGradient(request.elevationGain(), request.distance());

        User mockUser = userRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Mock user not found"));

        Route route = Route.builder()
                .user(mockUser)
                .name(request.name())
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
}
