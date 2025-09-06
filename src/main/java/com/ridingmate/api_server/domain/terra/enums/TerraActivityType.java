package com.ridingmate.api_server.domain.terra.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum TerraActivityType {

    BIKING(1, "Biking"),
    HANDBIKING(14, "Handbiking"),
    MOUNTAIN_BIKING(15, "Mountain Biking"),
    ROAD_BIKING(16, "Road Biking"),
    SPINNING(17, "Spinning"),
    STATIONARY_BIKING(18, "Stationary Biking"),
    UTILITY_BIKING(19, "Utility Biking"),
    TRIATHLON(126, "Triathlon"),

    // 필요한 경우 다른 운동 타입 추가
    UNKNOWN(-1, "Unknown");

    private final int id;
    private final String description;

    private static final Set<Integer> CYCLING_IDS = Arrays.stream(values())
            .filter(type -> type != UNKNOWN)
            .map(TerraActivityType::getId)
            .collect(Collectors.toSet());

    public static boolean isCyclingActivity(int id) {
        return CYCLING_IDS.contains(id);
    }

    public static TerraActivityType fromId(int id) {
        return Arrays.stream(values())
                .filter(type -> type.id == id)
                .findFirst()
                .orElse(UNKNOWN);
    }
}