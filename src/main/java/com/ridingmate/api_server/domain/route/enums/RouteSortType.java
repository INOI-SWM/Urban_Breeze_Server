package com.ridingmate.api_server.domain.route.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor
@Schema(description = "루트 정렬 타입")
public enum RouteSortType {
    CREATED_AT_ASC("오래된순", Sort.by(Sort.Direction.ASC, "createdAt")),
    CREATED_AT_DESC("최신순", Sort.by(Sort.Direction.DESC, "createdAt")),
    DISTANCE_ASC("주행거리 오름차순", Sort.by(Sort.Direction.ASC, "totalDistance")),
    DISTANCE_DESC("주행거리 내림차순", Sort.by(Sort.Direction.DESC, "totalDistance"));

    @Schema(description = "정렬 타입 설명")
    private final String description;
    
    @Schema(description = "Spring Data JPA Sort 객체")
    private final Sort sort;
} 