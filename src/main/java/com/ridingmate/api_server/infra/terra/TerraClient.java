package com.ridingmate.api_server.infra.terra;

import com.ridingmate.api_server.global.exception.BusinessException;
import com.ridingmate.api_server.infra.terra.dto.request.TerraGenerateAuthLinkRequest;
import com.ridingmate.api_server.infra.terra.dto.request.TerraProviderAuthRequest;
import com.ridingmate.api_server.infra.terra.dto.response.TerraGenerateAuthLinkResponse;
import com.ridingmate.api_server.infra.terra.dto.response.TerraProviderAuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.CodecException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

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

    public TerraProviderAuthResponse generateProviderAuthLink(TerraProviderAuthRequest request, TerraProvider terraProvider){
        return terraWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/auth/authenticateUser")
                        .queryParam("resource", terraProvider)
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
                .bodyToMono(TerraProviderAuthResponse.class)
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

    public void retrieveActivity(UUID terraUserId, LocalDate startDate){
        terraWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/activity")
                        .queryParam("user_id", terraUserId)
                        .queryParam("start_date", startDate)
                        .queryParam("end_date", LocalDate.now())
                        .queryParam("to_webhook", true)
                        .queryParam("with_samples", true)
                        .build())
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    if (response.statusCode().is4xxClientError()) {
                                        return Mono.error(new TerraException(TerraErrorCode.TERRA_REQUEST_FAILED));
                                    }
                                    return Mono.error(new TerraException(TerraErrorCode.TERRA_SERVER_ERROR));
                                })
                )
                .bodyToMono(TerraProviderAuthResponse.class)
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

    /**
     * Terra 사용자 연동 해제
     * @param terraUserId Terra 사용자 ID
     */
    public void deauthenticateUser(UUID terraUserId) {
        log.info("Terra 사용자 연동 해제 시작: terraUserId={}", terraUserId);
        
        terraWebClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/auth/deauthenticateUser")
                        .queryParam("user_id", terraUserId)
                        .build())
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Terra 사용자 연동 해제 실패: terraUserId={}, status={}, body={}", 
                                        terraUserId, response.statusCode(), errorBody);
                                    if (response.statusCode().is4xxClientError()) {
                                        return Mono.error(new TerraException(TerraErrorCode.TERRA_REQUEST_FAILED));
                                    }
                                    return Mono.error(new TerraException(TerraErrorCode.TERRA_SERVER_ERROR));
                                })
                )
                .bodyToMono(String.class)
                .onErrorMap(
                        throwable -> !(throwable instanceof BusinessException),
                        throwable -> {
                            if (throwable instanceof CodecException) {
                                return new TerraException(TerraErrorCode.TERRA_MAPPING_FAILED);
                            }
                            return new TerraException(TerraErrorCode.TERRA_CONNECTION_FAILED);
                        }
                )
                .doOnSuccess(response -> log.info("Terra 사용자 연동 해제 완료: terraUserId={}", terraUserId))
                .block();
    }
}
