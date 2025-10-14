package com.ridingmate.api_server.infra.kakao;

import com.ridingmate.api_server.global.exception.BusinessException;
import com.ridingmate.api_server.infra.kakao.dto.request.KakaoSearchRequest;
import com.ridingmate.api_server.infra.kakao.dto.response.KakaoSearchResponse;
import com.ridingmate.api_server.infra.kakao.dto.response.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class KakaoClient {

    private final WebClient kakaoWebClient;
    private final WebClient kakaoApiWebClient;

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 15;

    public KakaoSearchResponse searchPlaces(KakaoSearchRequest request) {
        return kakaoWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/keyword.json")
                        .queryParamIfPresent("query", Optional.ofNullable(request.query()))
                        .queryParamIfPresent("x", Optional.ofNullable(request.x()))
                        .queryParamIfPresent("y", Optional.ofNullable(request.y()))
                        .queryParam("page", DEFAULT_PAGE)
                        .queryParam("size", DEFAULT_SIZE)
                        .build())
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            if (response.statusCode().is4xxClientError()) {
                                return Mono.error(new KakaoException(KakaoErrorCode.KAKAO_REQUEST_FAILED));
                            }
                            return Mono.error(new KakaoException(KakaoErrorCode.KAKAO_SERVER_ERROR));
                        })
                )
                .bodyToMono(KakaoSearchResponse.class)
                .onErrorMap(
                    throwable -> !(throwable instanceof BusinessException),
                    throwable -> new KakaoException(KakaoErrorCode.KAKAO_CONNECTION_FAILED)
                )
                .block();
    }

    /**
     * Kakao 사용자 정보 조회
     *
     * @param accessToken Kakao Access Token
     * @return KakaoUserInfoResponse 사용자 정보
     */
    public KakaoUserInfoResponse getUserInfo(String accessToken) {
        return kakaoApiWebClient.get()
            .uri("/v2/user/me")
            .headers(headers -> headers.setBearerAuth(accessToken))
            .retrieve()
            .onStatus(
                status -> status.is4xxClientError() || status.is5xxServerError(),
                response -> response.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        if (response.statusCode().is4xxClientError()) {
                            return Mono.error(new KakaoException(KakaoErrorCode.KAKAO_REQUEST_FAILED));
                        }
                        return Mono.error(new KakaoException(KakaoErrorCode.KAKAO_SERVER_ERROR));
                    })
            )
            .bodyToMono(KakaoUserInfoResponse.class)
            .onErrorMap(
                throwable -> !(throwable instanceof BusinessException),
                throwable -> new KakaoException(KakaoErrorCode.KAKAO_CONNECTION_FAILED)
            )
            .block();
    }
} 