package com.ridingmate.api_server.domain.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "활동 이미지 삭제 응답")
public class DeleteActivityImageResponse {

    @Schema(description = "삭제된 이미지 ID", example = "1")
    private final Long deletedImageId;

    public static DeleteActivityImageResponse from(Long imageId) {
        return new DeleteActivityImageResponse(imageId);
    }
}
