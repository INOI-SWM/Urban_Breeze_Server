package com.ridingmate.api_server.infra.kakao.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record KakaoSearchResponse(
        Meta meta,
        List<Document> documents
) {
    
    @Builder
    public record Meta(
            Integer total_count,
            Integer pageable_count,
            Boolean is_end,
            RegionInfo region_info
    ) {
    }
    
    @Builder
    public record RegionInfo(
            String region,
            String keyword,
            Boolean selected_region
    ) {
    }
    
    @Builder
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