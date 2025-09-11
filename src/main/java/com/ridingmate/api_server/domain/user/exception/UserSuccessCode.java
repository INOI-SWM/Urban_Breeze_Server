package com.ridingmate.api_server.domain.user.exception;

import com.ridingmate.api_server.global.exception.BaseCode;
import com.ridingmate.api_server.global.exception.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserSuccessCode implements SuccessCode {

    GET_MY_INFO_SUCCESS(HttpStatus.OK, "내 정보 조회 성공"),
    UPDATE_NICKNAME_SUCCESS(HttpStatus.OK,  "닉네임 변경 성공"),
    UPDATE_INTRODUCE_SUCCESS(HttpStatus.OK, "한 줄 소개 변경 성공"),
    UPDATE_GENDER_SUCCESS(HttpStatus.OK, "성별 변경 성공"),
    UPDATE_BIRTH_YEAR_SUCCESS(HttpStatus.OK, "출생년도 변경 성공");

    private final HttpStatus status;
    private final String message;
}
