package com.ridingmate.api_server.domain.route.dto.request;

import com.ridingmate.api_server.global.client.dto.request.OrsRouteRequest;

import java.util.List;

public record RouteSegmentRequest(
        List<List<Double>> coordinates,
        boolean elevation
) {
    public OrsRouteRequest toOrsRequest() {
        return new OrsRouteRequest(this.coordinates, this.elevation);
    }
}