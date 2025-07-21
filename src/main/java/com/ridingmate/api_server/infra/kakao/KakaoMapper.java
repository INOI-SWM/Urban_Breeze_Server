package com.ridingmate.api_server.infra.kakao;

import com.ridingmate.api_server.infra.kakao.dto.response.KakaoSearchResponse;
import com.ridingmate.api_server.domain.route.dto.response.MapSearchResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class KakaoMapper {

    public MapSearchResponse toMapSearchResponse(KakaoSearchResponse kakaoResponse) {
        List<MapSearchResponse.Document> documents = kakaoResponse.documents()
                .stream()
                .map(this::toDocument)
                .collect(Collectors.toList());

        MapSearchResponse.BoundingBox bbox = calculateBoundingBox(kakaoResponse.documents());

        return MapSearchResponse.of(bbox, documents);
    }

    private MapSearchResponse.Document toDocument(KakaoSearchResponse.Document document) {
        return MapSearchResponse.Document.from(
                document.place_name(),
                document.distance(),
                document.place_url(),
                document.address_name(),
                document.phone(),
                document.category_group_name(),
                document.x(),
                document.y()
        );
    }

    private MapSearchResponse.BoundingBox calculateBoundingBox(List<KakaoSearchResponse.Document> documents) {
        if (documents.isEmpty()) {
            // 검색 결과가 없을 때 기본 BoundingBox 반환 (정상 응답)
            return MapSearchResponse.BoundingBox.of(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }

        double minLon = Double.MAX_VALUE;
        double maxLon = Double.MIN_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        
        double sumLon = 0.0;
        double sumLat = 0.0;

        for (KakaoSearchResponse.Document document : documents) {
            double lon = parseDouble(document.x());
            double lat = parseDouble(document.y());
            
            minLon = Math.min(minLon, lon);
            maxLon = Math.max(maxLon, lon);
            minLat = Math.min(minLat, lat);
            maxLat = Math.max(maxLat, lat);
            
            sumLon += lon;
            sumLat += lat;
        }

        double midLon = sumLon / documents.size();
        double midLat = sumLat / documents.size();

        return MapSearchResponse.BoundingBox.of(
                minLon, minLat, maxLon, maxLat, midLon, midLat
        );
    }

    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new KakaoException(KakaoErrorCode.KAKAO_INVALID_COORDINATE);
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new KakaoException(KakaoErrorCode.KAKAO_INVALID_COORDINATE);
        }
    }
} 