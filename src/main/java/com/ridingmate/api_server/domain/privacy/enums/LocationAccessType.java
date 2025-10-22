package com.ridingmate.api_server.domain.privacy.enums;

/**
 * 위치정보 조회 유형
 */
public enum LocationAccessType {
    /**
     * 위치정보 수집 (GPS 데이터 수집)
     */
    COLLECTION("수집"),
    
    /**
     * 위치정보 이용 (내부 처리/분석)
     */
    USE("이용"),
    
    /**
     * 위치정보 조회 (사용자 본인 조회)
     */
    ACCESS("조회"),
    
    /**
     * 위치정보 다운로드 (파일 다운로드)
     */
    DOWNLOAD("다운로드"),
    
    /**
     * 위치정보 제공 (제3자에게 제공)
     */
    PROVISION("제공");

    private final String description;

    LocationAccessType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
