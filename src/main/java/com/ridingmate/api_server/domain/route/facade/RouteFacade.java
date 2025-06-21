package com.ridingmate.api_server.domain.route.facade;

import com.ridingmate.api_server.domain.route.dto.request.RouteSegmentRequest;
import com.ridingmate.api_server.domain.route.dto.response.RouteSegmentResponse;
import com.ridingmate.api_server.global.client.OrsClient;
import com.ridingmate.api_server.global.client.OrsMapper;
import com.ridingmate.api_server.global.client.dto.response.OrsRouteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RouteFacade {

    private final OrsClient orsClient;

    public RouteSegmentResponse generateSegment(RouteSegmentRequest request) {
        OrsRouteResponse orsResponse = orsClient.getRoutePreview(request.toOrsRequest());
        return OrsMapper.toRouteSegmentResponse(orsResponse);
    }
}
