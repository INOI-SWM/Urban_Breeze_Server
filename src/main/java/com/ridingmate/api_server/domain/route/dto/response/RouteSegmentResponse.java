package com.ridingmate.api_server.domain.route.dto.response;

import java.util.List;

public record RouteSegmentResponse(
        List<Double> bbox,
        List<List<Double>> geometry,
        int totalDuration,
        double totalDistance,
        double averageGradient
) {
}
