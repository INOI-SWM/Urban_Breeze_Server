package com.ridingmate.api_server.domain.activity.dto.request;

import com.ridingmate.api_server.domain.activity.enums.ActivitySortType;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springdoc.core.annotations.ParameterObject;

@ParameterObject
public record ActivityListRequest(
        @Parameter(
                description = "페이지 번호 (기본 값: 0)",
                example = "0",
                schema = @Schema(type = "integer", defaultValue = "0")
        )
        int page,

        @Parameter(
                description = "페이지 크기 (기본 값: 20)",
                example = "20",
                schema = @Schema(type = "integer", defaultValue = "20")
        )
        int size,

        @Parameter(
                description = "정렬 타입 (기본 값: 최신순)",
                example = "STARTED_AT_DESC",
                schema = @Schema(defaultValue = "STARTED_AT_DESC")
        )
        ActivitySortType sortType
) {
    public ActivityListRequest {
        // 기본값 설정 (primitive type이므로 null 체크 불필요)
        if (sortType == null) sortType = ActivitySortType.STARTED_AT_DESC;
    }
}
