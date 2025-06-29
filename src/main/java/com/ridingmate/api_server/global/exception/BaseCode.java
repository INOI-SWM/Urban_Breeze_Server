package com.ridingmate.api_server.global.exception;

import org.springframework.http.HttpStatus;

public interface BaseCode {
    HttpStatus getStatus();

    String getMessage();
}
