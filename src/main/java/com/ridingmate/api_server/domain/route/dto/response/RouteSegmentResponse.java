package com.ridingmate.api_server.domain.route.dto.response;

import java.util.List;

public record RouteSegmentResponse(
        List<Double> bbox,
        List<List<Double>> geometry,
        double totalDistance,
        double totalDuration,
        double averageGradient
) {
}
