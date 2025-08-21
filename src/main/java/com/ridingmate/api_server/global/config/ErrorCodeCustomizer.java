package com.ridingmate.api_server.global.config;

import com.ridingmate.api_server.global.exception.ApiErrorCodeExample;
import com.ridingmate.api_server.global.exception.ErrorCode;
import com.ridingmate.api_server.global.exception.ErrorResponse;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import lombok.Builder;
import lombok.Getter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;

@Component
public class ErrorCodeCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        Set<ApiErrorCodeExample> errorCodeExamples = AnnotatedElementUtils.findMergedRepeatableAnnotations(
            handlerMethod.getMethod(), ApiErrorCodeExample.class);

        if (!errorCodeExamples.isEmpty()) {
            errorCodeExamples.forEach(errorCodeExample -> {
                generateErrorCodeResponseExample(operation, errorCodeExample.value());
            });
        }

        return operation;
    }

    private void generateErrorCodeResponseExample(Operation operation, Class<? extends ErrorCode> type){
        ApiResponses responses = operation.getResponses();

        ErrorCode[] errorCodes = type.getEnumConstants();

        Map<Integer, List<ExampleHolder>> statusWithExampleHolders =
            Arrays.stream(errorCodes)
                .map(
                    errorCode -> {
                        try {
                            return ExampleHolder.builder()
                                .holder(
                                    getSwaggerExample(
                                        errorCode
                                    )
                                )
                                .code(errorCode.getStatus().value())
                                .name(errorCode.getCode())
                                .build();
                        } catch (NoSuchFieldError e) {
                            throw new RuntimeException(e);
                        }
                    })
                .collect(groupingBy(ExampleHolder::getCode));
        addExamplesToResponses(responses, statusWithExampleHolders);
    }

    private Example getSwaggerExample(ErrorCode errorCode) {
        ErrorResponse errorResponse = ErrorResponse.of(errorCode);
        Example example = new Example();
        example.setValue(errorResponse);
        return example;
    }

    private void addExamplesToResponses(ApiResponses responses, Map<Integer, List<ExampleHolder>> statusWithExampleHolders) {
        statusWithExampleHolders.forEach(
            (status,  v) -> {
                Content content  = new Content();
                MediaType mediaType = new MediaType();

                ApiResponse apiResponse = new ApiResponse();

                v.forEach(
                    exampleHolder -> mediaType.addExamples(
                        exampleHolder.getName(), exampleHolder.getHolder()));

                content.addMediaType("application/json", mediaType);
                apiResponse.setContent(content);

                responses.addApiResponse(status.toString(), apiResponse);

            }
        );
    }

    @Getter
    @Builder
    public static class ExampleHolder {
        private Example holder;
        private String name;
        private int code;
    }
}
