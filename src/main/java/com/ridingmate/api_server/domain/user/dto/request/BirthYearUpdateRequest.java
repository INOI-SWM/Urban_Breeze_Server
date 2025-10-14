package com.ridingmate.api_server.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "출생년도 변경 요청 DTO")
public record BirthYearUpdateRequest(
        @NotNull(message = "출생년도를 입력해주세요.")
        @Min(value = 1900, message = "유효한 출생년도를 입력해주세요.")
        @Max(value = 2024, message = "유효한 출생년도를 입력해주세요.")
        @Schema(description = "새로운 출생년도", example = "1995")
        Integer birthYear
) {
}
