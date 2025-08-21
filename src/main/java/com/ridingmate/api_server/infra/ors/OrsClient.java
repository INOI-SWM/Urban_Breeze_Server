package com.ridingmate.api_server.infra.ors;

import com.ridingmate.api_server.global.exception.BusinessException;
import com.ridingmate.api_server.infra.ors.dto.request.OrsRouteRequest;
import com.ridingmate.api_server.infra.ors.dto.response.OrsRouteResponse;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.CodecException;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class OrsClient {

    private final WebClient orsWebClient;

    public OrsRouteResponse getRoutePreview(OrsRouteRequest request) {
        return orsWebClient.post()
                .uri("/v2/directions/cycling-regular/geojson")
                .bodyValue(request)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            //4xx, 5xx 에러 처리
                            if (response.statusCode().is4xxClientError()) {
                                return Mono.error(new OrsException(OrsErrorCode.ORS_REQUEST_FAILED));
                            }
                            return Mono.error(new OrsException(OrsErrorCode.ORS_SERVER_ERROR));
                        })
                )
                .bodyToMono(OrsRouteResponse.class)
                .onErrorMap(
                    throwable -> !(throwable instanceof BusinessException),
                    throwable -> {
                        if (throwable instanceof CodecException) {
                            log.error("[ORS] API Response Mapping Failed", throwable);
                            return new OrsException(OrsErrorCode.ORS_MAPPING_FAILED);
                        }
                        log.error("[ORS] API Call - Network or Unknown Error", throwable);
                        return new OrsException(OrsErrorCode.ORS_CONNECTION_FAILED);
                    }
                )
                .block();
    }
}
