package com.ridingmate.api_server.domain.route.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@Schema(description = "추천 코스 복사 요청")
public class CopyRecommendedRouteRequest {

    @NotNull(message = "경로 ID는 필수입니다.")
    @Schema(description = "복사할 경로 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID routeId;

    @Schema(description = "새로운 경로 제목 (선택사항)", example = "내가 만든 한강 라이딩 코스")
    private String newTitle;

    @Builder
    private CopyRecommendedRouteRequest(UUID routeId, String newTitle) {
        this.routeId = routeId;
        this.newTitle = newTitle;
    }
}
