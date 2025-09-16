package com.ridingmate.api_server.domain.activity.exception;

import com.ridingmate.api_server.global.exception.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ActivitySuccessCode implements SuccessCode {
    ACTIVITY_LIST_FETCHED(HttpStatus.OK, "활동 목록이 조회되었습니다."),
    ACTIVITY_DETAIL_FETCHED(HttpStatus.OK, "활동 세부 정보가 조회되었습니다."),
    ACTIVITY_STATS_FETCHED(HttpStatus.OK, "활동 통계가 조회되었습니다."),
    ACTIVITY_CREATED(HttpStatus.CREATED, "활동이 생성되었습니다."),
    ACTIVITY_UPDATED(HttpStatus.OK, "활동이 수정되었습니다."),
    ACTIVITY_DELETED(HttpStatus.OK, "활동이 삭제되었습니다."),
    ACTIVITY_IMAGE_UPLOADED(HttpStatus.CREATED, "활동 이미지가 업로드되었습니다."),
    ACTIVITY_IMAGE_DELETED(HttpStatus.OK, "활동 이미지가 삭제되었습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
