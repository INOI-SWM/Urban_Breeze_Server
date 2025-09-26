package com.ridingmate.api_server.domain.route.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@Schema(description = "내 경로에 추가 요청")
public class AddRouteToMyRoutesRequest {

    @NotNull(message = "경로 ID는 필수입니다.")
    @Schema(description = "추가할 경로 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID routeId;

    @Builder
    private AddRouteToMyRoutesRequest(UUID routeId) {
        this.routeId = routeId;
    }
}
