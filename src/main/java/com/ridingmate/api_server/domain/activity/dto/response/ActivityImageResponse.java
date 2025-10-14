package com.ridingmate.api_server.domain.activity.dto.response;

import com.ridingmate.api_server.domain.activity.entity.ActivityImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "활동 이미지 응답")
public class ActivityImageResponse {

    @Schema(description = "이미지 ID", example = "1")
    private final Long id;

    @Schema(description = "이미지 URL", example = "https://s3.amazonaws.com/bucket/activity-image/activity_1_uuid.jpg")
    private final String imageUrl;

    @Schema(description = "표시 순서", example = "1")
    private final Integer displayOrder;

    public static ActivityImageResponse of(ActivityImage image, String imageUrl) {
        return new ActivityImageResponse(image.getId(), imageUrl, image.getDisplayOrder());
    }
}
