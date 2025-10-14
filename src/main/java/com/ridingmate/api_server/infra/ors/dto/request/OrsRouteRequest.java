package com.ridingmate.api_server.infra.ors.dto.request;

import java.util.List;

public record OrsRouteRequest(
        List<List<Double>> coordinates,
        boolean elevation
) {}
