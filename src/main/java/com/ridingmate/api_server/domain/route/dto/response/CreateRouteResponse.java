package com.ridingmate.api_server.domain.route.dto.response;

public record CreateRouteResponse(
        Long routeId,
        String title,
        long totalDuration,
        double totalDistance,
        double totalElevationGain
) {
}
