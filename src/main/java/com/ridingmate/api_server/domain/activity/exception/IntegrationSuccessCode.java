package com.ridingmate.api_server.domain.activity.exception;

import com.ridingmate.api_server.global.exception.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum IntegrationSuccessCode implements SuccessCode {
    INTEGRATION_AUTHENTICATION_SUCCESS(HttpStatus.OK, "기록 연동을 위한 인증 링크 생성이 완료되었습니다."),
    INTEGRATION_RETRIEVE_ACTIVITY_SUCCESS(HttpStatus.ACCEPTED, "기록 연동 요청이 완료되었습니다."),
    INTEGRATION_TERRA_AUTH_TOKEN_SUCCESS(HttpStatus.OK, "Terra SDK 인증 토큰이 발급되었습니다."),
    INTEGRATION_API_USAGE_SUCCESS(HttpStatus.OK, "API 사용량 조회가 완료되었습니다."),
    INTEGRATION_API_USAGE_INCREMENT_SUCCESS(HttpStatus.OK, "API 사용량이 증가되었습니다.")
    ;
    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return this.status;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
