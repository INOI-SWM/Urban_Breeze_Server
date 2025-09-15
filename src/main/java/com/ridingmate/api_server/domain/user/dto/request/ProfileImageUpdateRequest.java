package com.ridingmate.api_server.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "프로필 이미지 수정 요청")
public record ProfileImageUpdateRequest(
        @NotNull(message = "프로필 이미지 파일은 필수입니다")
        @Schema(description = "프로필 이미지 파일", type = "string", format = "binary")
        MultipartFile profileImage
) {
}
