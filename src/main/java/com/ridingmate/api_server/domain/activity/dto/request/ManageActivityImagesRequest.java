package com.ridingmate.api_server.domain.activity.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springdoc.core.annotations.ParameterObject;

import java.util.List;

@Schema(description = "활동 이미지 관리 요청")
public record ManageActivityImagesRequest(
        @Schema(description = "활동 ID", example = "1")
        @NotNull(message = "활동 ID는 필수입니다")
        Long activityId,
        
        @Schema(description = "이미지 메타데이터 목록", 
                examples = {
                    "기존 이미지 순서 변경: {\"imageId\": 1, \"displayOrder\": 2, \"isDeleted\": false, \"fileName\": null}",
                    "새 이미지 추가: {\"imageId\": null, \"displayOrder\": 1, \"isDeleted\": false, \"fileName\": \"new_image.jpg\"}",
                    "기존 이미지 삭제: {\"imageId\": 1, \"displayOrder\": 1, \"isDeleted\": true, \"fileName\": null}"
                })
        List<ImageMetaInfo> images
) {
    
    @Schema(description = "이미지 메타데이터")
    public record ImageMetaInfo(
            @Schema(description = "기존 이미지 ID (신규 이미지면 null)", example = "1")
            Long imageId,
            
            @Schema(description = "이미지 표시 순서 (1부터 시작)", example = "1")
            @NotNull(message = "표시 순서는 필수입니다")
            Integer displayOrder,
            
            @Schema(description = "삭제 여부", example = "false")
            @NotNull(message = "삭제 여부는 필수입니다")
            Boolean isDeleted,
            
            @Schema(description = "파일명 (새 이미지 매핑용, 기존 이미지면 null)", example = "new_image.jpg")
            String fileName
    ) {
        /**
         * 기존 이미지인지 확인
         */
        @Schema(hidden = true)
        public boolean isExistingImage() {
            return imageId != null && !isDeleted;
        }
        
        /**
         * 삭제할 이미지인지 확인
         */
        @Schema(hidden = true)
        public boolean isImageToDelete() {
            return imageId != null && isDeleted;
        }
    }
}
