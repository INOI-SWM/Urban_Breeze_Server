package com.ridingmate.api_server.domain.activity.dto.response;

import com.ridingmate.api_server.domain.activity.entity.Activity;
import com.ridingmate.api_server.global.dto.PaginationResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "활동 목록 응답")
public record ActivityListResponse(
        @Schema(description = "활동 목록")
        List<ActivityListItemResponse> activities,

        @Schema(description = "페이지네이션 정보")
        PaginationResponse pagination
) {
    /**
     * ActivityListItemResponse 리스트와 Activity 페이지로부터 ActivityListResponse 생성
     * @param activityItems 변환된 활동 목록
     * @param activityPage Activity 페이지 (페이지네이션 정보 추출용)
     * @return ActivityListResponse DTO
     */
    public static ActivityListResponse of(List<ActivityListItemResponse> activityItems, Page<Activity> activityPage) {
        PaginationResponse pagination = PaginationResponse.from(activityPage);
        return new ActivityListResponse(activityItems, pagination);
    }
}
