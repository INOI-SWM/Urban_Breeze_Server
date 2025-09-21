package com.ridingmate.api_server.domain.activity.dto.response;

import com.ridingmate.api_server.domain.activity.entity.ActivityImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
@Schema(description = "활동 이미지 업로드 응답")
public class UploadActivityImagesResponse {

    @Schema(description = "업로드된 이미지 정보 목록")
    private final List<ActivityImageResponse> uploadedImages;

    @Schema(description = "업로드된 이미지 개수")
    private final int uploadedCount;

    public static UploadActivityImagesResponse from(List<ActivityImage> activityImages) {
        List<ActivityImageResponse> imageResponses = activityImages.stream()
                .map(ActivityImageResponse::from)
                .toList();
        
        return new UploadActivityImagesResponse(imageResponses, imageResponses.size());
    }
}
