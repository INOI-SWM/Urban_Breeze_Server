package com.ridingmate.api_server.domain.activity.exception.code;

import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ActivityImageErrorCode implements ErrorCode {
    ACTIVITY_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "ACTIVITY_IMAGE_NOT_FOUND", "활동 이미지를 찾을 수 없습니다."),
    ACTIVITY_IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ACTIVITY_IMAGE_UPLOAD_FAILED", "활동 이미지 업로드에 실패했습니다."),
    ACTIVITY_IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ACTIVITY_IMAGE_DELETE_FAILED", "활동 이미지 삭제에 실패했습니다."),
    INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_IMAGE_FORMAT", "지원하지 않는 이미지 형식입니다."),
    IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "IMAGE_SIZE_EXCEEDED", "이미지 크기가 제한을 초과했습니다."),
    MAX_IMAGE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "MAX_IMAGE_COUNT_EXCEEDED", "활동당 최대 이미지 개수를 초과했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
