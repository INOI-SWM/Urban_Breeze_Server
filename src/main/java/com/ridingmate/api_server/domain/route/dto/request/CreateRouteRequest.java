package com.ridingmate.api_server.domain.route.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;

public record CreateRouteRequest(

        @NotBlank
        String name,

        @NotEmpty
        String polyline,

        @NotNull
        Double distance,

        @NotNull
        Duration duration,

        @NotNull
        Double elevationGain
) {}
