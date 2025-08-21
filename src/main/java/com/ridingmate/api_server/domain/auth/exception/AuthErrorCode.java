package com.ridingmate.api_server.domain.auth.exception;

import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    /**
     * JWT 토큰 관련 에러코드들
     */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN", "만료된 토큰입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "UNSUPPORTED_TOKEN", "지원되지 않는 토큰입니다."),
    TOKEN_VALIDATION_ERROR(HttpStatus.UNAUTHORIZED, "TOKEN_VALIDATION_ERROR", "토큰 검증 중 오류가 발생했습니다."),
    MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "MALFORMED_TOKEN", "잘못된 형식의 토큰입니다."),
    EMPTY_TOKEN(HttpStatus.UNAUTHORIZED, "EMPTY_TOKEN", "토큰이 비어있습니다."),
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "INVALID_SIGNATURE", "잘못된 토큰 서명입니다."),

    /**
     * 인증/인가 관련 에러코드들
     */
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "인증에 실패했습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "접근이 거부되었습니다."),
    /**
     * 인증 정보(e.g., JWT, Firebase UID)는 유효하나,
     * 해당 정보에 매핑되는 사용자가 우리 DB에 존재하지 않을 때 (e.g., 탈퇴, DB 누락)
     */
    AUTHENTICATION_USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_USER_NOT_FOUND", "인증된 사용자를 찾을 수 없습니다."),

    /**
     * Refresh Token 관련 에러코드들
     */
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_REFRESH_TOKEN", "만료된 리프레시 토큰입니다."),
    USED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "USED_REFRESH_TOKEN", "이미 사용된 리프레시 토큰입니다."),
    REVOKED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "REVOKED_REFRESH_TOKEN", "무효화된 리프레시 토큰입니다."),

    /**
     * 기타 에러코드
     */
    UNKNOWN_AUTH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "UNKNOWN_AUTH_ERROR", "알 수 없는 인증 오류가 발생했습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
