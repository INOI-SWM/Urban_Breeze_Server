package com.ridingmate.api_server.domain.route.facade;

import com.ridingmate.api_server.domain.route.dto.request.CreateRouteRequest;
import com.ridingmate.api_server.domain.route.dto.request.RouteSegmentRequest;
import com.ridingmate.api_server.domain.route.dto.response.CreateRouteResponse;
import com.ridingmate.api_server.domain.route.dto.response.RouteSegmentResponse;
import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.route.service.RouteService;
import com.ridingmate.api_server.infra.aws.s3.S3Manager;
import com.ridingmate.api_server.infra.geoapify.GeoapifyClient;
import com.ridingmate.api_server.infra.ors.OrsClient;
import com.ridingmate.api_server.infra.ors.OrsMapper;
import com.ridingmate.api_server.infra.ors.dto.response.OrsRouteResponse;
import com.ridingmate.api_server.global.util.GeometryUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class RouteFacade {

    private final OrsClient orsClient;
    private final GeoapifyClient geoapifyClient;

    private final RouteService routeService;
    private final S3Manager s3Manager;

    public RouteSegmentResponse generateSegment(RouteSegmentRequest request) {
        OrsRouteResponse orsResponse = orsClient.getRoutePreview(request.toOrsRequest());
        return OrsMapper.toRouteSegmentResponse(orsResponse);
    }

    public CreateRouteResponse createRoute(CreateRouteRequest request) {
        LineString routeLine = GeometryUtil.polylineToLineString(request.polyline());
        byte[] thumbnailBytes = geoapifyClient.getStaticMap(routeLine);
        Route route = routeService.createRoute(request, routeLine);
        s3Manager.uploadByteFiles(route.getThumbnailImagePath(), thumbnailBytes);

        double distanceKm = new BigDecimal(route.getTotalDistance())
                .setScale(2, RoundingMode.DOWN)
                .doubleValue();

        return new CreateRouteResponse(
                route.getId(),
                route.getName(),
                route.getTotalDuration().toMinutes(),
                distanceKm,
                route.getTotalElevationGain()
                );
    }
}
