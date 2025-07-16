package com.ridingmate.api_server.infra.kakao.dto.request;

import lombok.Builder;

@Builder
public record KakaoSearchRequest(
        String query,
        Double x,
        Double y
) {
}