package com.ridingmate.api_server.global.exception;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ApiErrorCodeExamples.class)
public @interface ApiErrorCodeExample {
    Class<? extends ErrorCode> value();
}
