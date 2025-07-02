package com.ridingmate.api_server.global.util;

import org.locationtech.jts.geom.*;

import java.util.List;
import java.util.stream.Collectors;

public class GeometryUtil {

    private static final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * Encoded polyline을 LineString으로 변환 예시: LINESTRING (126.9706 37.5547, 127.0276 37.4979, ...)
     */
    public static LineString polylineToLineString(String polyline) {
        List<Coordinate> coordinates = PolylineDecoder.decode(polyline);
        return factory.createLineString(coordinates.toArray(new Coordinate[0]));
    }

    /**
     * LineString의 BBOX(Envelope) 반환
     */
    public static Envelope getBoundingBox(LineString line) {
        return line.getEnvelopeInternal();
    }

    /**
     * BBOX를 기반으로 중심 좌표 계산
     */
    public static Coordinate getCenterCoordinate(Envelope bbox) {
        double centerLon = (bbox.getMinX() + bbox.getMaxX()) / 2.0; // 경도
        double centerLat = (bbox.getMinY() + bbox.getMaxY()) / 2.0; // 위도
        return new Coordinate(centerLon, centerLat);
    }

    /**
     * 한 번에: Polyline으로부터 중심 좌표 반환
     */
    public static Coordinate getCenterFromPolyline(String polyline) {
        LineString line = polylineToLineString(polyline);
        Envelope bbox = getBoundingBox(line);
        return getCenterCoordinate(bbox);
    }

    /**
     * BBOX로부터 줌레벨 추정
     */
    public static int getZoomLevel(Envelope bbox) {
        double lonDiff = bbox.getMaxX() - bbox.getMinX();
        double latDiff = bbox.getMaxY() - bbox.getMinY();
        double maxDiff = Math.max(lonDiff, latDiff);

        double safetyFactor = 0.8;

        double adjustedDiff = maxDiff / safetyFactor;

        // 경도 차이를 기준으로 줌레벨 추정
        double zoom = Math.log(360 / adjustedDiff) / Math.log(2);

        // 최소,최대 줌레벨 제한
        int zoomLevel = Math.max(2, Math.min(20, (int) Math.floor(zoom)));

        return zoomLevel;
    }

    public static int getZoomLevelFromPolyline(String polyline) {
        LineString line = polylineToLineString(polyline);
        Envelope bbox = getBoundingBox(line);
        return getZoomLevel(bbox);
    }

    /**
     * Coordinates 리스트를 Geoapify geometry 파라미터용 polyline 문자열로 변환
     * 예: polyline:127.0535,37.53560,127.05349,37.53559 ...
     */
    public static String toGeoapifyPolyline(List<Coordinate> coordinates) {
        return coordinates.stream()
                .map(coord -> String.format("%.7f,%.7f", coord.x, coord.y)) // 소수점 7자리로 lon, lat
                .collect(Collectors.joining(","));
    }
}
