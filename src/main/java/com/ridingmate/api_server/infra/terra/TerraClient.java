package com.ridingmate.api_server.infra.terra;

import com.ridingmate.api_server.global.exception.BusinessException;
import com.ridingmate.api_server.infra.terra.dto.request.TerraGenerateAuthLinkRequest;
import com.ridingmate.api_server.infra.terra.dto.response.TerraGenerateAuthLinkResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.CodecException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class TerraClient {

    private final WebClient terraWebClient;

    public TerraGenerateAuthLinkResponse generateAuthLink(TerraGenerateAuthLinkRequest request){
        return terraWebClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("/v2/auth/generateWidgetSession")
                .build())
            .bodyValue(request)
            .retrieve()
            .onStatus(
                status -> status.is4xxClientError() || status.is5xxServerError(),
                response -> response.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        //4xx, 5xx 에러 처리
                        if (response.statusCode().is4xxClientError()) {
                            return Mono.error(new TerraException(TerraErrorCode.TERRA_REQUEST_FAILED));
                        }
                        return Mono.error(new TerraException(TerraErrorCode.TERRA_SERVER_ERROR));
                    })
            )
            .bodyToMono(TerraGenerateAuthLinkResponse.class)
            .onErrorMap(
                throwable -> !(throwable instanceof BusinessException),
                throwable -> {
                    if (throwable instanceof CodecException) {
                        return new TerraException(TerraErrorCode.TERRA_MAPPING_FAILED);
                    }
                    return new TerraException(TerraErrorCode.TERRA_CONNECTION_FAILED);
                }
            )
            .block();
    }
}
