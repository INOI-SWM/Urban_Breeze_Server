package com.ridingmate.api_server.domain.activity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityProvider {
    APPLE_HEALTH_KIT("APPLE_HEALTH_KIT", "Apple HealthKit"),
    GARMIN("GARMIN", "Garmin"),
    SAMSUNG_HEALTH("SAMSUNG_HEALTH", "Samsung Health"),
    GOOGLE_FIT("GOOGLE_FIT", "Google Fit"),
    STRAVA("STRAVA", "Strava"),
    SUUNTO("SUUNTO", "Suunto"),
    WAHOO("WAHOO", "Wahoo"),
    UNKNOWN("UNKNOWN", "Unknown");

    private final String code;
    private final String displayName;

    /**
     * 코드로부터 ActivityProvider 찾기
     * @param code 제공자 코드
     * @return ActivityProvider (찾지 못하면 UNKNOWN)
     */
    public static ActivityProvider fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return UNKNOWN;
        }
        
        for (ActivityProvider provider : values()) {
            if (provider.code.equalsIgnoreCase(code)) {
                return provider;
            }
        }
        
        return UNKNOWN;
    }
}

