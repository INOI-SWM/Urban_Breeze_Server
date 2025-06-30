package com.ridingmate.api_server.infra.ors;

import com.ridingmate.api_server.infra.ors.dto.request.OrsRouteRequest;
import com.ridingmate.api_server.infra.ors.dto.response.OrsRouteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class OrsClient {

    private final WebClient orsWebClient;

    @Value("${ors.api-key}")
    private String apiKey;

    public OrsRouteResponse getRoutePreview(OrsRouteRequest request) {
        return orsWebClient.post()
                .uri("/v2/directions/cycling-regular/geojson")
                .header("Authorization", apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OrsRouteResponse.class)
                .block();
    }
}
