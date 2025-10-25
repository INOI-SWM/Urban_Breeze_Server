package com.ridingmate.api_server.domain.route.dto.response;

/**
 * TCX 파일 다운로드 정보 DTO
 */
public record TcxDownloadInfo(
        String fileName,
        String contentType,
        byte[] content
) {
    public static TcxDownloadInfo of(byte[] content, String fileName) {
        return new TcxDownloadInfo(fileName, "application/vnd.garmin.tcx+xml", content);
    }
}
