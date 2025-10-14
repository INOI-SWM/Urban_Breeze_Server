package com.ridingmate.api_server.domain.activity.exception;

import com.ridingmate.api_server.global.exception.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ActivitySuccessCode implements SuccessCode {
    ACTIVITY_LIST_FETCHED(HttpStatus.OK, "주행 기록 목록이 조회되었습니다."),
    ACTIVITY_DETAIL_FETCHED(HttpStatus.OK, "주행 기록 세부 정보가 조회되었습니다."),
    ACTIVITY_STATS_FETCHED(HttpStatus.OK, "주행 기록 통계가 조회되었습니다."),
    ACTIVITY_CREATED(HttpStatus.CREATED, "주행 기록이 생성되었습니다."),
    ACTIVITY_UPDATED(HttpStatus.OK, "주행 기록이 수정되었습니다."),
    ACTIVITY_DELETED(HttpStatus.OK, "주행 기록이 삭제되었습니다."),
    ACTIVITY_IMAGE_ADDED(HttpStatus.CREATED, "주행 기록 이미지가 추가되었습니다."),
    ACTIVITY_IMAGE_UPLOADED(HttpStatus.CREATED, "주행 기록 이미지가 업로드되었습니다."),
    ACTIVITY_IMAGE_DELETED(HttpStatus.OK, "주행 기록 이미지가 삭제되었습니다."),
    ACTIVITY_TITLE_UPDATED(HttpStatus.OK, "주행 기록 제목이 변경되었습니다."),
    ACTIVITY_IMPORTED(HttpStatus.CREATED, "Apple 운동 기록이 업로드되었습니다."),
    TERRA_AUTH_TOKEN_GENERATED(HttpStatus.OK, "Terra 인증 토큰이 발급되었습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
