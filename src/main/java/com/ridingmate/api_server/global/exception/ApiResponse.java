package com.ridingmate.api_server.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ApiResponse<T> {

    private int status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String code;

    private String message;

    private T data;

    public static <T> ApiResponse<T> success(SuccessCode successCode, T data) {
        return ApiResponse.<T>builder()
                .status(successCode.getStatus().value())
                .message(successCode.getMessage())
                .data(data)
                .build();
    }
    public static <T> ApiResponse<T> success(SuccessCode code) {
        return success(code, null);
    }

    public static <T> ApiResponse<T> error(ErrorCode code, T data) {
        return ApiResponse.<T>builder()
                .status(code.getStatus().value())
                .code(code.getCode())
                .message(code.getMessage())
                .data(data)
                .build();
    }
}
