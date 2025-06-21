package com.ridingmate.api_server.global.client;

import com.ridingmate.api_server.domain.route.dto.response.RouteSegmentResponse;
import com.ridingmate.api_server.global.client.dto.response.OrsRouteResponse;

public class OrsMapper {
    public static RouteSegmentResponse toRouteSegmentResponse(OrsRouteResponse response) {
        OrsRouteResponse.Feature feature = response.features().get(0);
        OrsRouteResponse.Properties props = feature.properties();
        OrsRouteResponse.Summary summary = props.summary();

        return new RouteSegmentResponse(
                feature.geometry().coordinates(),
                summary.distance(),
                summary.duration(),
                props.ascent()
        );
    }
}
