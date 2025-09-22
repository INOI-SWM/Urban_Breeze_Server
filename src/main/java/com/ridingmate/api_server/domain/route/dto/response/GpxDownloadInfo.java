package com.ridingmate.api_server.domain.route.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * GPX 파일 다운로드 정보를 담는 DTO
 */
@Schema(description = "GPX 파일 다운로드 정보")
public record GpxDownloadInfo(
    @Schema(description = "GPX 파일 내용", example = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>...")
    byte[] content,
    
    @Schema(description = "파일명", example = "한강_자전거길.gpx")
    String fileName,
    
    @Schema(description = "Content-Type", example = "application/gpx+xml")
    String contentType
) {
    public static GpxDownloadInfo of(byte[] content, String fileName) {
        return new GpxDownloadInfo(
                content,
                fileName,
                "application/gpx+xml"
        );
    }
}
