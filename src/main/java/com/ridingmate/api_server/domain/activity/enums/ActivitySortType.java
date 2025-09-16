package com.ridingmate.api_server.domain.activity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor
public enum ActivitySortType {
    CREATED_AT_DESC("최신순", Sort.by(Sort.Direction.DESC, "startedAt")),
    CREATED_AT_ASC("오래된순", Sort.by(Sort.Direction.ASC, "startedAt")),
    DISTANCE_ASC("주행거리 오름차순", Sort.by(Sort.Direction.ASC, "distance")),
    DISTANCE_DESC("주행거리 내림차순", Sort.by(Sort.Direction.DESC, "distance"));
    ;

    private final String description;
    private final Sort sort;
}
