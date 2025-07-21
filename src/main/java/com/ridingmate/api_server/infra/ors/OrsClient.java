package com.ridingmate.api_server.infra.ors;

import com.ridingmate.api_server.infra.ors.dto.request.OrsRouteRequest;
import com.ridingmate.api_server.infra.ors.dto.response.OrsRouteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
public class OrsClient {

    private final WebClient orsWebClient;

    public OrsRouteResponse getRoutePreview(OrsRouteRequest request) {
        return orsWebClient.post()
                .uri("/v2/directions/cycling-regular/geojson")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OrsRouteResponse.class)
                .onErrorMap(
                        throwable -> new OrsException(OrsErrorCode.ORS_SERVER_CALL_FAILED)
                )
                .block();
    }
}
