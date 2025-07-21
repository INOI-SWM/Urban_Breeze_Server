package com.ridingmate.api_server.infra.kakao.dto.response;

import java.util.List;

public record KakaoSearchResponse(
        Meta meta,
        List<Document> documents
) {

    public record Meta(
            Integer total_count,
            Integer pageable_count,
            Boolean is_end,
            SameName same_name
    ) {
    }

    public record SameName(
            String keyword,
            List<String> region,
            String selected_region
    ) {
    }

    public record Document(
            String place_name,
            String distance,
            String place_url,
            String category_name,
            String address_name,
            String road_address_name,
            String id,
            String phone,
            String category_group_code,
            String category_group_name,
            String x,
            String y
    ) {
    }
} 