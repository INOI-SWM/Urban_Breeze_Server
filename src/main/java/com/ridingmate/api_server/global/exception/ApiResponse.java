package com.ridingmate.api_server.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ApiResponse<T> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String code;

    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ErrorResponse.FieldError> errors;

    public static <T> ApiResponse<T> success(SuccessCode successCode, T data) {
        return ApiResponse.<T>builder()
                .message(successCode.getMessage())
                .data(data)
                .build();
    }
    public static <T> ApiResponse<T> success(SuccessCode code) {
        return success(code, null);
    }

    public static <T> ApiResponse<T> error(ErrorCode code, List<ErrorResponse.FieldError> errors) {
        return ApiResponse.<T>builder()
                .code(code.getCode())
                .message(code.getMessage())
                .errors(errors)
                .build();
    }
}
