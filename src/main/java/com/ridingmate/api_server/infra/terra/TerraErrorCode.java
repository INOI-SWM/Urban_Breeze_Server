package com.ridingmate.api_server.infra.terra;

import com.ridingmate.api_server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TerraErrorCode implements ErrorCode {

    TERRA_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TERRA_REQUEST_FAILED", "TERRA API 요청 생성 실패"),

    TERRA_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "TERRA_SERVER_ERROR", "TERRA 서버 내부 오류 발생"),

    TERRA_CONNECTION_FAILED(HttpStatus.GATEWAY_TIMEOUT, "TERRA_CONNECTION_FAILED", "TERRA 서버 연결 실패"),

    TERRA_MAPPING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TERRA_MAPPING_FAILED", "TERRA 응답 처리 실패"),

    // SQS 관련 에러 코드
    TERRA_S3_URL_EXPIRED(HttpStatus.GONE, "TERRA_S3_URL_EXPIRED", "Terra S3 URL이 만료되었습니다"),

    TERRA_S3_URL_CLIENT_ERROR(HttpStatus.BAD_REQUEST, "TERRA_S3_URL_CLIENT_ERROR", "Terra S3 URL 클라이언트 에러"),

    TERRA_S3_URL_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "TERRA_S3_URL_SERVER_ERROR", "Terra S3 URL 서버 에러"),

    TERRA_PAYLOAD_DOWNLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TERRA_PAYLOAD_DOWNLOAD_FAILED", "Terra 페이로드 다운로드 실패");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
