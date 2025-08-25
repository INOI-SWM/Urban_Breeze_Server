package com.ridingmate.api_server.global.util;

import com.ridingmate.api_server.domain.route.exception.RouteException;
import com.ridingmate.api_server.domain.route.exception.code.RouteCreationErrorCode;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import java.util.List;
import java.util.stream.Collectors;

public class GeometryUtil {

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    private static final double DISTANCE_TOLERANCE = 0.00000000001;

    /**
     * Encoded polyline을 LineString으로 변환 예시: LINESTRING (126.9706 37.5547, 127.0276 37.4979, ...)
     */
    public static LineString polylineToLineString(String polyline) {
        try {
            List<Coordinate> coordinates = PolylineDecoder.decode(polyline);
            if (coordinates.size() < 2) {
                throw new RouteException(RouteCreationErrorCode.ROUTE_NOT_ENOUGH_POINTS);
            }
            return geometryFactory.createLineString(coordinates.toArray(new Coordinate[0]));
        } catch (Exception e) {
            throw new RouteException(RouteCreationErrorCode.ROUTE_POLYLINE_INVALID);
        }
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

    /**
     * Coordinates 리스트를 Geoapify geometry 파라미터용 polyline 문자열로 변환
     * 예: polyline:127.0535,37.53560,127.05349,37.53559 ...
     */
    public static String toGeoapifyPolyline(List<Coordinate> coordinates) {
        return coordinates.stream()
                .map(coord -> String.format("%.7f,%.7f", coord.x, coord.y)) // 소수점 7자리로 lon, lat
                .collect(Collectors.joining(","));
    }

    /**
     * LineString에서 출발 좌표 반환
     */
    public static Coordinate getStartCoordinate(LineString lineString) {
        if (lineString != null && !lineString.isEmpty()) {
            return lineString.getCoordinateN(0);
        }
        return null;
    }

    /**
     * LineString에서 도착 좌표 반환
     */
    public static Coordinate getEndCoordinate(LineString lineString) {
        if (lineString != null && !lineString.isEmpty()) {
            return lineString.getCoordinateN(lineString.getNumPoints() - 1);
        }
        return null;
    }

    /**
     * LineString에서 모든 좌표 반환
     */
    public static List<Coordinate> getAllCoordinates(LineString lineString) {
        if (lineString != null && !lineString.isEmpty()) {
            return List.of(lineString.getCoordinates());
        }
        return List.of();
    }

    /**
     * 두 좌표 간의 거리 계산 (Haversine 공식 사용, km 단위)
     */
    public static Double calculateDistance(Double lon1, Double lat1, Double lon2, Double lat2) {
        if (lon1 == null || lat1 == null || lon2 == null || lat2 == null) {
            return null;
        }

        final double R = 6371; // 지구 반지름 (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        // 소수점 2자리로 반올림
        return Math.round(distance * 100.0) / 100.0;
    }
    public static List<Coordinate> simplifyRoute(Coordinate[] coordinates) {
        LineString lineString = geometryFactory.createLineString(coordinates);

        DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(lineString);
        simplifier.setDistanceTolerance(DISTANCE_TOLERANCE);

        Coordinate[] simplifiedCoords = simplifier.getResultGeometry().getCoordinates();

        return List.of(simplifiedCoords);
    }
}
