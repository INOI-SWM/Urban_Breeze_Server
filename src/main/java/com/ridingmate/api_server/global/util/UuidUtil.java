package com.ridingmate.api_server.global.util;

import java.util.UUID;

/**
 * UUID 관련 유틸리티 클래스
 */
public class UuidUtil {

    /**
     * 순차적 삽입을 위한 UUID 생성
     * 타임스탬프 부분을 앞으로 이동시켜 인덱스 성능을 향상시킴
     * 
     * @return 재배열된 UUID 문자열 (하이픈 제거)
     */
    public static String generateOrderedUuid() {
        String uuid = UUID.randomUUID().toString();
        return rearrangeUuid(uuid);
    }

    /**
     * UUID를 순차적 삽입에 최적화된 형태로 재배열
     * 
     * 원본: 58e0a7d7-eebc-11d8-9669-0800200c9a66
     * 결과: 11d8eebc58e0a7d796690800200c9a66
     * 
     * @param uuid 원본 UUID 문자열
     * @return 재배열된 UUID 문자열 (하이픈 제거)
     */
    private static String rearrangeUuid(String uuid) {
        String cleanUuid = uuid.replace("-", "");

        return cleanUuid.substring(12, 16) +  // 11d8 (version + timestamp high)
               cleanUuid.substring(8, 12) +   // eebc (timestamp mid)
               cleanUuid.substring(0, 8) +    // 58e0a7d7 (timestamp low)
               cleanUuid.substring(16, 20) +  // 9669 (clock sequence)
               cleanUuid.substring(20);       // 0800200c9a66 (node)
    }
}
