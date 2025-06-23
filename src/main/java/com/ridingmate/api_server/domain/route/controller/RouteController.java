package com.ridingmate.api_server.domain.route.controller;

import com.ridingmate.api_server.domain.route.dto.request.CreateRouteRequest;
import com.ridingmate.api_server.domain.route.dto.request.RouteSegmentRequest;
import com.ridingmate.api_server.domain.route.dto.response.RouteSegmentResponse;
import com.ridingmate.api_server.domain.route.facade.RouteFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteFacade routeFacade;

    @PostMapping("/segment")
    public ResponseEntity<RouteSegmentResponse> previewRoute(@RequestBody RouteSegmentRequest request) {
        RouteSegmentResponse response = routeFacade.generateSegment(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> createRoute(@RequestBody CreateRouteRequest request) {
        routeFacade.createRoute(request);
        return ResponseEntity.ok().build();
    }
}
