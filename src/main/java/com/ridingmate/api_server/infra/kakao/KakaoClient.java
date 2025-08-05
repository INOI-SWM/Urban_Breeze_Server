package com.ridingmate.api_server.infra.kakao;

import com.ridingmate.api_server.infra.kakao.dto.request.KakaoSearchRequest;
import com.ridingmate.api_server.infra.kakao.dto.response.KakaoSearchResponse;
import com.ridingmate.api_server.infra.kakao.dto.response.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@RequiredArgsConstructor
public class KakaoClient {

    private final WebClient kakaoWebClient;

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
                .bodyToMono(KakaoSearchResponse.class)
                .onErrorMap(throwable -> new KakaoException(KakaoErrorCode.KAKAO_SERVER_CALL_FAILED))
                .block();
    }

    /**
     * Kakao 사용자 정보 조회
     *
     * @param accessToken Kakao Access Token
     * @return KakaoUserInfoResponse 사용자 정보
     */
    public KakaoUserInfoResponse getUserInfo(String accessToken) {
        return WebClient.builder()
                .baseUrl("https://kapi.kakao.com")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .build()
                .get()
                .uri("/v2/user/me")
                .retrieve()
                .bodyToMono(KakaoUserInfoResponse.class)
                .onErrorMap(throwable -> new KakaoException(KakaoErrorCode.KAKAO_SERVER_CALL_FAILED))
                .block();
    }
} 