package com.ridingmate.api_server.infra.terra;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "테라 지원 서비스 목록")
public enum TerraProvider {
    STRAVA("STRAVA", "Strava"),
    GARMIN("GARMIN", "Garmin")
    ;

    private final String providerName;
    private final String displayName;


    /**
     * 제공자 이름으로 TerraProvider 찾기
     * @param providerName 제공자 이름
     * @return TerraProvider
     * @throws IllegalArgumentException 지원하지 않는 제공자 이름일 때
     */
    public static TerraProvider fromProviderName(String providerName) {
        for (TerraProvider provider : values()) {
            if (provider.providerName.equalsIgnoreCase(providerName)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 제공자입니다: " + providerName);
    }
}
