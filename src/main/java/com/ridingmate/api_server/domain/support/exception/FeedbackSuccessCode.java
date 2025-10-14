package com.ridingmate.api_server.domain.support.exception;

import com.ridingmate.api_server.global.exception.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FeedbackSuccessCode implements SuccessCode {
    FEEDBACK_CREATED(HttpStatus.CREATED, "피드백이 등록되었습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}