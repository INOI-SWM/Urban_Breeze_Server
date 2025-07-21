package com.ridingmate.api_server.infra.kakao.dto.request;

public record KakaoSearchRequest(
        String query,
        Double x,
        Double y
) {
    public static KakaoSearchRequest from(String query, Double longitude, Double latitude) {
        return new KakaoSearchRequest(query, longitude, latitude);
    }
}