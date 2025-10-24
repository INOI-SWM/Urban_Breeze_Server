package com.ridingmate.api_server.domain.route.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 경로 Waypoint 타입 열거형
 * TCX 파일의 waypoint 타입을 정의
 */
@Getter
@RequiredArgsConstructor
public enum WaypointType {
    
    // 기본 안내 타입
    GENERIC("generic", "일반", "일반적인 안내 지점"),
    SUMMIT("summit", "정상", "산봉우리 정상"),
    VALLEY("valley", "계곡", "계곡 또는 최저 지점"),
    
    // 보급/안전 타입
    WATER("water", "물", "물/식수 공급 지점"),
    FOOD("food", "보급", "음식/보급 지점"),
    DANGER("danger", "위험", "위험 지역 경고"),
    FIRST_AID("firstAid", "응급처치", "구급/응급 처치 지점"),
    
    // 방향 안내 타입
    LEFT("left", "좌회전", "좌회전 안내"),
    RIGHT("right", "우회전", "우회전 안내"),
    STRAIGHT("straight", "직진", "직진 안내"),
    
    // 사이클링 클라이밍 등급
    CATEGORY_4("category4", "4등급 오르막", "사이클링 클라이밍 4등급"),
    CATEGORY_3("category3", "3등급 오르막", "사이클링 클라이밍 3등급"),
    CATEGORY_2("category2", "2등급 오르막", "사이클링 클라이밍 2등급"),
    CATEGORY_1("category1", "1등급 오르막", "사이클링 클라이밍 1등급"),
    HORS_CATEGORY("horsCategory", "무제한급 오르막", "HC (Hors Catégorie), 가장 어려운 등급"),
    
    // 경쟁 타입
    SPRINT("sprint", "스프린트", "스프린트 구간 시작 지점");
    
    private final String code;
    private final String koreanName;
    private final String description;
    
    /**
     * 코드로 WaypointType 찾기
     * @param code 타입 코드
     * @return 해당하는 WaypointType, 없으면 GENERIC
     */
    public static WaypointType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return GENERIC;
        }
        
        for (WaypointType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        
        // 알 수 없는 타입은 GENERIC으로 처리
        return GENERIC;
    }
    
    /**
     * 유효한 타입인지 확인
     * @param code 타입 코드
     * @return 유효한 타입이면 true
     */
    public static boolean isValidType(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        
        for (WaypointType type : values()) {
            if (type.code.equals(code)) {
                return true;
            }
        }
        
        return false;
    }
}
