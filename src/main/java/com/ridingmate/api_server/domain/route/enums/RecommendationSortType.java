package com.ridingmate.api_server.domain.route.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor
@Schema(description = "추천 코스 정렬 타입")
public enum RecommendationSortType {
    NEAREST("가까운 순", "사용자 위치에서 가까운 순으로 정렬"),
    DISTANCE_LONG("거리 긴 순", "거리가 긴 순으로 정렬"),
    DISTANCE_SHORT("거리 짧은 순", "거리가 짧은 순으로 정렬"),
    DIFFICULTY_HIGH("난이도 높은 순", "난이도가 높은 순으로 정렬"),
    DIFFICULTY_LOW("난이도 낮은 순", "난이도가 낮은 순으로 정렬")
    ;

    @Schema(description = "정렬 타입 이름")
    private final String displayName;

    @Schema(description = "정렬 타입 설명")
    private final String description;

    /**
     * 정렬 타입에 따른 Sort 객체 반환
     * @return Sort 객체
     */
    public Sort getSort() {
        return switch (this) {
            case NEAREST -> Sort.by("id").ascending();
            case DISTANCE_LONG -> Sort.by("distance").descending();
            case DISTANCE_SHORT -> Sort.by("distance").ascending();
            case DIFFICULTY_HIGH -> Sort.by("difficulty").descending();
            case DIFFICULTY_LOW -> Sort.by("difficulty").ascending();
        };
    }
} 