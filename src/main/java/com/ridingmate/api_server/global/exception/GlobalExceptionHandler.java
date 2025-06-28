package com.ridingmate.api_server.global.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.file.AccessDeniedException;
import java.util.List;

/**
 * 전역 예외 처리 핸들러
 * 모든 Controller 단에서 발생할 수 있는 예외를 통합 처리하여
 * 일관된 응답 포맷(ApiResponse<ErrorResponse>)으로 반환한다.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * @RequestBody 유효성 검사 실패 (javax.validation.Valid 등) 시 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException", e);
        List<ErrorResponse.FieldError> fieldErrors = ErrorResponse.FieldError.of(e.getBindingResult());
        return ResponseEntity
                .status(GlobalErrorCode.VALIDATION_ERROR.getStatus())
                .body(ApiResponse.error(GlobalErrorCode.VALIDATION_ERROR, fieldErrors));
    }

    /**
     * @ModelAttribute 등에서 유효성 검사 실패 시 발생
     */
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleBindException(BindException e) {
        log.error("BindException", e);
        List<ErrorResponse.FieldError> fieldErrors = ErrorResponse.FieldError.of(e.getBindingResult());
        return ResponseEntity
                .status(GlobalErrorCode.VALIDATION_ERROR.getStatus())
                .body(ApiResponse.error(GlobalErrorCode.VALIDATION_ERROR, fieldErrors));
    }

    /**
     * @RequestParam 등에서 enum 타입과 매칭 실패 시 발생
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException", e);
        return ResponseEntity
                .status(GlobalErrorCode.BAD_REQUEST.getStatus())
                .body(ApiResponse.error(GlobalErrorCode.BAD_REQUEST, null));
    }

    /**
     * 지원하지 않는 HTTP 메서드 호출 시 발생 (ex. GET → POST만 지원)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException", e);
        return ResponseEntity
                .status(GlobalErrorCode.METHOD_NOT_ALLOWED.getStatus())
                .body(ApiResponse.error(GlobalErrorCode.METHOD_NOT_ALLOWED, null));
    }

    /**
     * 인증은 되었으나 권한이 없는 경우 발생 (403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleAccessDenied(AccessDeniedException e) {
        log.error("AccessDeniedException", e);
        return ResponseEntity
                .status(GlobalErrorCode.FORBIDDEN.getStatus())
                .body(ApiResponse.error(GlobalErrorCode.FORBIDDEN, null));
    }

    /**
     * 비즈니스 로직에서 명시적으로 발생시킨 커스텀 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleBusiness(BusinessException e) {
        log.error("BusinessException", e);
        ErrorCode code = e.getErrorCode();
        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResponse.error(code, null));
    }

    /**
     * @Validated @RequestParam, @PathVariable 등에 유효성 검사 실패 시 발생
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleConstraintViolation(ConstraintViolationException e) {
        log.error("ConstraintViolationException", e);
        return ResponseEntity
                .status(GlobalErrorCode.VALIDATION_ERROR.getStatus())
                .body(ApiResponse.error(GlobalErrorCode.VALIDATION_ERROR, null));
    }

    /**
     * 위에서 처리되지 않은 나머지 모든 예외에 대한 최종 예외 처리
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleGeneric(Exception e) {
        log.error("Unhandled Exception", e);
        return ResponseEntity
                .status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.error(GlobalErrorCode.INTERNAL_SERVER_ERROR, null));
    }
}