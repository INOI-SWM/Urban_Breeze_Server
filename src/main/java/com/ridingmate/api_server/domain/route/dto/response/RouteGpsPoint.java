package com.ridingmate.api_server.domain.route.dto.response;

import org.locationtech.jts.geom.Coordinate;

public record RouteGpsPoint(
    Double latitude,
    Double longitude,
    Double elevation
) {

    public static RouteGpsPoint from(Coordinate coordinate) {
        Double elevation = Double.isNaN(coordinate.getZ()) ? null : coordinate.getZ();
        return new RouteGpsPoint(coordinate.getY(), coordinate.getX(), elevation);
    }
}
