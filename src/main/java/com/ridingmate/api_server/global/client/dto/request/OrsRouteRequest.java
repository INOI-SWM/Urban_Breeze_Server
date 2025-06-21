package com.ridingmate.api_server.global.client.dto.request;

import java.util.List;

public record OrsRouteRequest(
        List<List<Double>> coordinates,
        boolean elevation
) {}
