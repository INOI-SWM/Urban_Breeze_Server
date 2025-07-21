package com.ridingmate.api_server.domain.route.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "지도 검색 응답")
public record MapSearchResponse(
        @Schema(description = "검색 결과 영역 정보", example = "검색 결과가 분포된 지역의 경계 좌표")
        BoundingBox bbox,
        
        @Schema(description = "검색된 장소 목록")
        List<Document> documents
) {
    
    public static MapSearchResponse of(BoundingBox bbox, List<Document> documents) {
        return new MapSearchResponse(bbox, documents);
    }
    @Schema(description = "검색 결과 영역 경계 좌표")
    public record BoundingBox(
            @Schema(description = "최소 경도", example = "126.970")
            Double minLon,
            
            @Schema(description = "최소 위도", example = "37.560")
            Double minLat,
            
            @Schema(description = "최대 경도", example = "126.980")
            Double maxLon,
            
            @Schema(description = "최대 위도", example = "37.570")
            Double maxLat,
            
            @Schema(description = "중심점 경도", example = "126.975")
            Double midLon,
            
            @Schema(description = "중심점 위도", example = "37.565")
            Double midLat
    ) {
        
        public static BoundingBox of(Double minLon, Double minLat, Double maxLon, Double maxLat, Double midLon, Double midLat) {
            return new BoundingBox(minLon, minLat, maxLon, maxLat, midLon, midLat);
        }
    }

    @Schema(description = "검색된 장소 정보")
    public record Document(
            @Schema(description = "장소명", example = "스타벅스 강남점")
            String place_name,
            
            @Schema(description = "중심좌표까지의 거리(단위: m)", example = "150")
            String distance,
            
            @Schema(description = "장소 상세페이지 URL", example = "http://place.map.kakao.com/12345")
            String place_url,
            
            @Schema(description = "지번 주소", example = "서울 강남구 역삼동 123-4")
            String address_name,
            
            @Schema(description = "전화번호", example = "02-1234-5678")
            String phone,
            
            @Schema(description = "카테고리 그룹명", example = "카페")
            String category_group_name,
            
            @Schema(description = "X좌표(경도)", example = "126.9780")
            String x,
            
            @Schema(description = "Y좌표(위도)", example = "37.5665")
            String y
    ){
        
        public static Document from(String place_name, String distance, String place_url, String address_name, String phone, String category_group_name, String x, String y) {
            return new Document(place_name, distance, place_url, address_name, phone, category_group_name, x, y);
        }
    }
}
