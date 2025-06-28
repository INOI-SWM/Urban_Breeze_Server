package com.ridingmate.api_server.domain.route.dto.response;

public record CreateRouteResponse(
        Long routeId,
        String name,
        long totalDuration,
        double totalDistance,
        double totalElevationGain
) {
}
