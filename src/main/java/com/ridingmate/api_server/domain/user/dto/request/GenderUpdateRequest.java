package com.ridingmate.api_server.domain.user.dto.request;

import com.ridingmate.api_server.domain.user.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "성별 변경 요청 DTO")
public record GenderUpdateRequest(
        @NotNull(message = "성별을 입력해주세요.")
        @Schema(description = "새로운 성별", example = "MALE")
        Gender gender
) {
}
