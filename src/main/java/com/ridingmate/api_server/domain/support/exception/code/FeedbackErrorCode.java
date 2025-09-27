package com.ridingmate.api_server.domain.support.exception.code;

import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FeedbackErrorCode implements ErrorCode {
    FEEDBACK_NOT_FOUND(HttpStatus.NOT_FOUND, "FEEDBACK_NOT_FOUND", "피드백을 찾을 수 없습니다."),
    FEEDBACK_ACCESS_DENIED(HttpStatus.FORBIDDEN, "FEEDBACK_ACCESS_DENIED", "해당 피드백에 접근 권한이 없습니다."),
    FEEDBACK_ALREADY_PROCESSED(HttpStatus.CONFLICT, "FEEDBACK_ALREADY_PROCESSED", "이미 처리된 피드백입니다."),
    FEEDBACK_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FEEDBACK_DELETE_FAILED", "피드백 삭제 중 오류가 발생했습니다."),
    INVALID_FEEDBACK_TITLE(HttpStatus.BAD_REQUEST, "INVALID_FEEDBACK_TITLE", "피드백 제목이 유효하지 않습니다."),
    INVALID_FEEDBACK_CONTENT(HttpStatus.BAD_REQUEST, "INVALID_FEEDBACK_CONTENT", "피드백 내용이 유효하지 않습니다."),
    INVALID_FEEDBACK_TYPE(HttpStatus.BAD_REQUEST, "INVALID_FEEDBACK_TYPE", "피드백 타입이 유효하지 않습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}