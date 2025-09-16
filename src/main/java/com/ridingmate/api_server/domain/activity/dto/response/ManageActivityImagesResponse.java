package com.ridingmate.api_server.domain.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "활동 이미지 관리 응답")
public record ManageActivityImagesResponse(
        @Schema(description = "활동 ID", example = "1")
        Long activityId,
        
        @Schema(description = "관리된 이미지 목록")
        List<ImageResult> images,
        
        @Schema(description = "처리 결과 요약")
        ProcessSummary summary
) {
    
    @Schema(description = "이미지 처리 결과")
    public record ImageResult(
            @Schema(description = "이미지 ID", example = "1")
            Long imageId,
            
            @Schema(description = "이미지 URL", example = "https://s3.amazonaws.com/bucket/activity-images/123.jpg")
            String imageUrl,
            
            @Schema(description = "표시 순서", example = "1")
            Integer displayOrder,
            
            @Schema(description = "처리 상태", example = "ADDED")
            ProcessStatus status
    ) {}
    
    @Schema(description = "처리 결과 요약")
    public record ProcessSummary(
            @Schema(description = "추가된 이미지 수", example = "2")
            Integer addedCount,
            
            @Schema(description = "삭제된 이미지 수", example = "1")
            Integer deletedCount,
            
            @Schema(description = "순서 변경된 이미지 수", example = "3")
            Integer reorderedCount,
            
            @Schema(description = "총 이미지 수", example = "5")
            Integer totalCount
    ) {}
    
    @Schema(description = "처리 상태")
    public enum ProcessStatus {
        @Schema(description = "새로 추가됨")
        ADDED,
        
        @Schema(description = "기존 이미지 유지")
        KEPT,
        
        @Schema(description = "순서 변경됨")
        REORDERED,
        
        @Schema(description = "삭제됨")
        DELETED
    }
    
    public static ManageActivityImagesResponse of(Long activityId, List<ImageResult> images, ProcessSummary summary) {
        return new ManageActivityImagesResponse(activityId, images, summary);
    }
}
