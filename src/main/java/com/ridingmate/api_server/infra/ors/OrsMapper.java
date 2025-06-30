package com.ridingmate.api_server.infra.ors;

import com.ridingmate.api_server.domain.route.dto.response.RouteSegmentResponse;
import com.ridingmate.api_server.infra.ors.dto.response.OrsRouteResponse;

public class OrsMapper {
    public static RouteSegmentResponse toRouteSegmentResponse(OrsRouteResponse response) {
        OrsRouteResponse.Feature feature = response.features().get(0);
        OrsRouteResponse.Properties props = feature.properties();
        OrsRouteResponse.Summary summary = props.summary();

        int durationMinutes = (int) Math.round(summary.duration() / 60.0);

        return new RouteSegmentResponse(
                feature.bbox(),
                feature.geometry().coordinates(),
                durationMinutes,
                summary.distance(),
                props.ascent()
        );
    }
}
