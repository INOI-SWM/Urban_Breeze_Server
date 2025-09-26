package com.ridingmate.api_server.domain.route.exception;

import com.ridingmate.api_server.global.exception.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RouteSuccessCode implements SuccessCode {
    SEGMENT_CREATED(HttpStatus.CREATED, "경로 세그먼트가 생성되었습니다."),
    ROUTE_CREATED(HttpStatus.CREATED, "경로가 생성되었습니다."),
    SHARE_LINK_FETCHED(HttpStatus.OK, "경로 공유 링크가 조회되었습니다."),
    ROUTE_LIST_FETCHED(HttpStatus.OK, "경로 목록이 조회되었습니다."),
    MAP_SEARCH_FETCHED(HttpStatus.OK, "장소 검색 결과 목록이 조회되었습니다."),
    ROUTE_DETAIL_FETCHED(HttpStatus.OK, "경로 세부 정보가 조회되었습니다."),
    ROUTE_ADDED_TO_MY_ROUTES(HttpStatus.OK, "내 경로에 추가되었습니다."),
    ROUTE_COPIED(HttpStatus.CREATED, "경로가 복사되었습니다."),
    ;
    private final HttpStatus status;
    private final String message;
}
