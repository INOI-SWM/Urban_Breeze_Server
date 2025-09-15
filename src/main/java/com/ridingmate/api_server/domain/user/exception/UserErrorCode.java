package com.ridingmate.api_server.domain.user.exception;

import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "유저를 찾을 수 없습니다."),
    INVALID_PROFILE_IMAGE(HttpStatus.BAD_REQUEST, "INVALID_PROFILE_IMAGE", "유효하지 않은 프로필 이미지입니다."),
    PROFILE_IMAGE_TOO_LARGE(HttpStatus.BAD_REQUEST, "PROFILE_IMAGE_TOO_LARGE", "프로필 이미지 크기가 너무 큽니다. (최대 20MB)"),
    INVALID_PROFILE_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_PROFILE_IMAGE_FORMAT", "지원하지 않는 이미지 형식입니다. (JPG, PNG, WebP만 지원)")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

}
