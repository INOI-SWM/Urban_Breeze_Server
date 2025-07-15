package com.ridingmate.api_server.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

@Schema(description = "페이지네이션 응답")
public record PaginationResponse(
        @Schema(description = "현재 페이지", example = "0")
        int currentPage,

        @Schema(description = "전체 페이지 수", example = "5")
        int totalPages,

        @Schema(description = "전체 요소 수", example = "15")
        long totalElements,

        @Schema(description = "페이지 크기", example = "3")
        int size,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,

        @Schema(description = "이전 페이지 존재 여부", example = "false")
        boolean hasPrevious
) {

    /**
     * Spring Data Page 객체로부터 PaginationResponse 생성
     * @param page Spring Data Page 객체
     * @return PaginationResponse DTO
     */
    public static PaginationResponse from(Page<?> page) {
        return new PaginationResponse(
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
} 