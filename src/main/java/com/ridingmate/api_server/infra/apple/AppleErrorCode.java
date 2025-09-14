package com.ridingmate.api_server.infra.apple;

import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AppleErrorCode implements ErrorCode {

    /**
     * 우리 서버의 로직 또는 설정 오류로 인해 Apple이 4xx 에러를 반환한 경우.
     * 사용자에게는 500 Internal Server Error로 응답해야 함.
     */
    APPLE_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "APPLE_REQUEST_FAILED", "Apple API 요청 생성 실패"),

    /**
     * Apple 서버가 5xx 에러로 응답한 경우. (Apple 서버 내부 오류)
     */
    APPLE_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "APPLE_SERVER_ERROR", "Apple 서버 내부 오류 발생"),

    /**
     * Apple 서버에 연결할 수 없거나 응답 시간 초과 등 네트워크 문제 발생.
     */
    APPLE_CONNECTION_FAILED(HttpStatus.GATEWAY_TIMEOUT, "APPLE_CONNECTION_FAILED", "Apple 서버 연결 실패"),

    /**
     * Apple JWKS 응답 데이터 매핑 실패
     */
    APPLE_JWKS_MAPPING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "APPLE_JWKS_MAPPING_FAILED", "Apple JWKS 응답 데이터 매핑 실패"),

    /**
     * 유효하지 않은 Key ID
     */
    APPLE_INVALID_KEY_ID(HttpStatus.INTERNAL_SERVER_ERROR, "APPLE_INVALID_KEY_ID", "유효하지 않은 Apple Key ID"),

    /**
     * RSA 공개키 변환 실패
     */
    APPLE_KEY_CONVERSION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "APPLE_KEY_CONVERSION_FAILED", "Apple RSA 공개키 변환 실패");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
