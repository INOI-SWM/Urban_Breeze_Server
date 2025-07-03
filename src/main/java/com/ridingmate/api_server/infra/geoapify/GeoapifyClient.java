package com.ridingmate.api_server.infra.geoapify;

import com.ridingmate.api_server.global.util.GeometryUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineString;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GeoapifyClient {

    private final GeoapifyProperty geoapifyProperty;

    @Qualifier("geoapifyWebClient")
    private final WebClient geoapifyWebClient;

    public byte[] getStaticMap(LineString lineString) {
        Envelope bbox = GeometryUtil.getBoundingBox(lineString);
        Coordinate center = GeometryUtil.getCenterCoordinate(bbox);
        String centerParam = String.format("lonlat:%.6f,%.6f", center.x, center.y);
        int zoom = GeometryUtil.getZoomLevel(bbox);

        List<Coordinate> coordinates = List.of(lineString.getCoordinates());
        String polyline = GeometryUtil.toGeoapifyPolyline(coordinates);
        String geometryParam = "polyline:" + polyline +";linecolor:%23ff0000;linewidth:3";

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
                .bodyToMono(byte[].class)
                .block();  // 동기 호출 (필요시 비동기로)
    }
}