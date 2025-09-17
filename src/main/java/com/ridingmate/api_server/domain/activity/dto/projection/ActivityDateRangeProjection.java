package com.ridingmate.api_server.domain.activity.dto.projection;

import java.time.LocalDateTime;

/**
 * 활동 날짜 범위 조회를 위한 Projection DTO
 * 첫 번째와 마지막 활동 날짜를 타입 안전하게 받기 위해 사용
 */
public record ActivityDateRangeProjection(
        LocalDateTime firstActivityDate,
        LocalDateTime lastActivityDate
) {
}
