package com.ridingmate.api_server.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {
    SELECT_SUCCESS(HttpStatus.OK, "200", "조회 요청이 성공했습니다."),
    DELETE_SUCCESS(HttpStatus.OK, "200", "삭제 요청이 성공했습니다."),
    INSERT_SUCCESS(HttpStatus.CREATED, "201", "삽입 요청이 성공했습니다."),
    UPDATE_SUCCESS(HttpStatus.NO_CONTENT, "204", "업데이트 요청이 성공했습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}

