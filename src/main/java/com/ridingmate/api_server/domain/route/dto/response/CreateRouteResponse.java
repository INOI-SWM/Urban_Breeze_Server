package com.ridingmate.api_server.domain.route.dto.response;

import java.time.Duration;

public record CreateRouteResponse(
        Long routeId,
        String name,
        long totalDuration,
        double totalDistance,
        double averageGradient
) {
}
