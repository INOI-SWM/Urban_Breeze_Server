package com.ridingmate.api_server.infra.geoapify;

import com.ridingmate.api_server.global.exception.BusinessException;
import com.ridingmate.api_server.global.util.GeometryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineString;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeoapifyClient {

    private final GeoapifyProperty geoapifyProperty;

    private final WebClient geoapifyWebClient;

    public byte[] getStaticMap(LineString lineString) {
        // 썸네일용으로 LineString 간소화 (URL 길이 제한 회피)
        LineString simplifiedLineString = GeometryUtil.simplifyForThumbnail(lineString);
        
        Envelope bbox = GeometryUtil.getBoundingBox(simplifiedLineString);
        Coordinate center = GeometryUtil.getCenterCoordinate(bbox);
        String centerParam = String.format("lonlat:%.6f,%.6f", center.x, center.y);
        int zoom = GeometryUtil.getZoomLevel(bbox);

        List<Coordinate> coordinates = List.of(simplifiedLineString.getCoordinates());
        String polyline = GeometryUtil.toGeoapifyPolyline(coordinates);
        String geometryParam = "polyline:" + polyline +";linecolor:%23ff0000;linewidth:3";
        
        log.debug("[Geoapify] 썸네일 생성: 원본 좌표 {}개 → 간소화 후 {}개", 
                lineString.getCoordinates().length, coordinates.size());

        return geoapifyWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .queryParam("style", "klokantech-basic")
                .queryParam("width", 600)
                .queryParam("height", 450)
                .queryParam("geometry", geometryParam)
                .queryParam("center", centerParam)
                .queryParam("zoom", zoom)
                .queryParam("apiKey", geoapifyProperty.apikey())
                .build())
            .retrieve()
            .onStatus(
                status -> status.is4xxClientError() || status.is5xxServerError(),
                response -> response.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        if (response.statusCode().is4xxClientError()) {
                            return Mono.error(new GeoapifyException(GeoapifyErrorCode.GEOAPIFY_REQUEST_FAILED));
                        }
                        return Mono.error(new GeoapifyException(GeoapifyErrorCode.GEOAPIFY_SERVER_ERROR));
                    })
            )
            .bodyToMono(byte[].class)
            .onErrorMap(
                throwable -> !(throwable instanceof BusinessException),
                throwable -> new GeoapifyException(GeoapifyErrorCode.GEOAPIFY_CONNECTION_FAILED)
            )
            .block();
    }
}