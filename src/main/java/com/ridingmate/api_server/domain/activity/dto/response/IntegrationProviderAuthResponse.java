package com.ridingmate.api_server.domain.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record IntegrationProviderAuthResponse(
        @Schema(description = "연동을 위한 url", example = "https://www.fitbit.com/oauth2/authorize?response_type=code&client_id=23BBG9&scope=settings+nutrition+sleep+heartrate+electrocardiogram+weight+respiratory_rate+oxygen_saturation+profile+temperature+cardio_fitness+activity+location&state=bLqqjPie9ptwoWm6VBxHCu6JkkoWJp")
        String url
) {
}
