package com.ridingmate.api_server.infra.kakao;

import com.ridingmate.api_server.infra.kakao.dto.request.KakaoSearchRequest;
import com.ridingmate.api_server.infra.kakao.dto.response.KakaoSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@RequiredArgsConstructor
public class KakaoClient {

    private final WebClient kakaoWebClient;

    private static final int DEFAULT_RADIUS = 20000;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 15;
    private static final String DEFAULT_SORT = "accuracy";

    public KakaoSearchResponse searchPlaces(KakaoSearchRequest request) {
        return kakaoWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/keyword.json")
                        .queryParamIfPresent("query", Optional.ofNullable(request.query()))
                        .queryParamIfPresent("x", Optional.ofNullable(request.x()))
                        .queryParamIfPresent("y", Optional.ofNullable(request.y()))
                        .queryParam("radius", DEFAULT_RADIUS)
                        .queryParam("page", DEFAULT_PAGE)
                        .queryParam("size", DEFAULT_SIZE)
                        .queryParam("sort", DEFAULT_SORT)
                        .build())
                .retrieve()
                .bodyToMono(KakaoSearchResponse.class)
                .onErrorMap(
                        throwable -> new KakaoException(KakaoErrorCode.KAKAO_SERVER_CALL_FAILED)
                )
                .block();
    }
} 