package com.ridingmate.api_server.domain.activity.exception;

import com.ridingmate.api_server.global.exception.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum IntegrationSuccessCode implements SuccessCode {
    INTEGRATION_AUTHENTICATION_SUCCESS(HttpStatus.OK, "기록 연동을 위한 인증 링크 생성이 완료되었습니다."),
    ;
    private final HttpStatus status;
    private final String message;
}
