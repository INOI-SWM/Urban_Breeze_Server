package com.ridingmate.api_server.domain.route.facade;

import com.ridingmate.api_server.domain.route.dto.request.CreateRouteRequest;
import com.ridingmate.api_server.domain.route.dto.request.RouteSegmentRequest;
import com.ridingmate.api_server.domain.route.dto.response.CreateRouteResponse;
import com.ridingmate.api_server.domain.route.dto.response.RouteSegmentResponse;
import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.route.service.RouteService;
import com.ridingmate.api_server.global.client.OrsClient;
import com.ridingmate.api_server.global.client.OrsMapper;
import com.ridingmate.api_server.global.client.dto.response.OrsRouteResponse;
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
    private final RouteService routeService;

    public RouteSegmentResponse generateSegment(RouteSegmentRequest request) {
        OrsRouteResponse orsResponse = orsClient.getRoutePreview(request.toOrsRequest());
        return OrsMapper.toRouteSegmentResponse(orsResponse);
    }

    public CreateRouteResponse createRoute(CreateRouteRequest request) {
        LineString routeLine = GeometryUtil.polylineToLineString(request.polyline());
        //TODO 썸네일 이미지 생성 및 추가 기능 구현 필요
        Route route = routeService.createRoute(request, routeLine);

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
