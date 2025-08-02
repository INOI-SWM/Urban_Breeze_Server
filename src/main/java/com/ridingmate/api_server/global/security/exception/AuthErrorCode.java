package com.ridingmate.api_server.global.security.exception;

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
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH002", "만료된 토큰입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH003", "지원되지 않는 토큰입니다."),
    TOKEN_VALIDATION_ERROR(HttpStatus.UNAUTHORIZED, "AUTH004", "토큰 검증 중 오류가 발생했습니다."),
    MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH005", "잘못된 형식의 토큰입니다."),
    EMPTY_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH006", "토큰이 비어있습니다."),
    
    /**
     * 인증 관련 에러코드들
     */
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH101", "인증에 실패했습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH102", "접근이 거부되었습니다."),
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH103", "사용자를 찾을 수 없습니다.");
    
    private final HttpStatus status;
    private final String code;
    private final String message;
} 