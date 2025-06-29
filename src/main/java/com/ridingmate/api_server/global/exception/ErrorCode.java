package com.ridingmate.api_server.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode extends BaseCode {
    String getCode();
}
