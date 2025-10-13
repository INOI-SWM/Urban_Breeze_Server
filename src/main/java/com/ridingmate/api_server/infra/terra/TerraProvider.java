package com.ridingmate.api_server.infra.terra;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "테라 지원 서비스 목록")
public enum TerraProvider {
    STRAVA("STRAVA", "Strava"),
    GARMIN("GARMIN", "Garmin"),
    SUUNTO("SUUNTO", "Suunto"),
    SAMSUNG_HEALTH("SAMSUNG-HEALTH", "Samsung"),
    HEALTH_CONNECT("HEALTH-CONNECT", "Google Fit(Health Connect)"),
    APPLE_HEALTH_KIT("APPLE-HEALTH-KIT", "Apple Health"),
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

    /**
     * Terra에서 오는 provider 이름을 우리 시스템의 providerName으로 매핑
     * @param terraProviderName Terra에서 오는 provider 이름
     * @return TerraProvider
     */
    public static TerraProvider fromTerraProviderName(String terraProviderName) {
        switch (terraProviderName.toUpperCase()) {
            case "SAMSUNG":
                return SAMSUNG_HEALTH;
            case "APPLE":
                return APPLE_HEALTH_KIT;
            case "HEALTH_CONNECT":
                return HEALTH_CONNECT;
            case "STRAVA":
                return STRAVA;
            case "GARMIN":
                return GARMIN;
            case "SUUNTO":
                return SUUNTO;
            default:
                throw new IllegalArgumentException("지원하지 않는 Terra 제공자입니다: " + terraProviderName);
        }
    }
}
