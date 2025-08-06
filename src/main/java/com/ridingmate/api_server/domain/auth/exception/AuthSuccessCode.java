package com.ridingmate.api_server.domain.auth.exception;

import com.ridingmate.api_server.global.exception.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthSuccessCode implements SuccessCode {
    GOOGLE_LOGIN_SUCCESS(HttpStatus.OK, "Google 로그인이 완료되었습니다."),
    APPLE_LOGIN_SUCCESS(HttpStatus.OK, "Apple 로그인이 완료되었습니다."),
    KAKAO_LOGIN_SUCCESS(HttpStatus.OK, "Kakao 로그인이 완료되었습니다."),
    TOKEN_REFRESH_SUCCESS(HttpStatus.OK, "토큰 갱신이 완료되었습니다.")
    ;
    private final HttpStatus status;
    private final String message;
} 