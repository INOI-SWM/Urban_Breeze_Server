package com.ridingmate.api_server.global.util;

import com.ggalmazor.ltdownsampling.DoublePoint;
import com.ggalmazor.ltdownsampling.LTThreeBuckets;
import com.ggalmazor.ltdownsampling.Point;
import com.ridingmate.api_server.domain.route.exception.RouteException;
import com.ridingmate.api_server.domain.route.exception.code.RouteCreationErrorCode;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GeometryUtil {

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    private static final double DISTANCE_TOLERANCE = 0.00000000001;

    /**
     * Coordinate 배열을 LineString으로 변환
     */
    public static LineString createLineStringFromCoordinates(Coordinate[] coordinates) {
        if (coordinates.length < 2) {
            throw new IllegalArgumentException("LineString을 생성하려면 최소 2개의 좌표가 필요합니다.");
        }
        return geometryFactory.createLineString(coordinates);
    }

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
     * LineString을 Google Polyline으로 변환 (Google Polyline Algorithm 사용)
     */
    public static String lineStringToPolyline(LineString lineString) {
        if (lineString == null || lineString.isEmpty()) {
            return "";
        }
        
        Coordinate[] coordinates = lineString.getCoordinates();
        return encodePolyline(coordinates);
    }

    /**
     * Coordinate 배열을 Google Polyline으로 인코딩
     */
    private static String encodePolyline(Coordinate[] coordinates) {
        StringBuilder encoded = new StringBuilder();
        
        int prevLat = 0;
        int prevLng = 0;
        
        for (Coordinate coordinate : coordinates) {
            // 좌표를 1e5로 스케일링하고 정수로 변환
            int lat = (int) Math.round(coordinate.y * 1e5);
            int lng = (int) Math.round(coordinate.x * 1e5);
            
            // 이전 좌표와의 차이 계산
            int deltaLat = lat - prevLat;
            int deltaLng = lng - prevLng;
            
            // 차이값을 인코딩
            encoded.append(encodeValue(deltaLat));
            encoded.append(encodeValue(deltaLng));
            
            // 현재 좌표를 이전 좌표로 업데이트
            prevLat = lat;
            prevLng = lng;
        }
        
        return encoded.toString();
    }

    /**
     * 단일 값을 Google Polyline 형식으로 인코딩
     */
    private static String encodeValue(int value) {
        // 1. 부호 비트를 LSB로 이동
        value = value < 0 ? ~(value << 1) : (value << 1);
        
        StringBuilder encoded = new StringBuilder();
        
        // 2. 5비트씩 처리
        while (value >= 0x20) {
            encoded.append((char) ((0x20 | (value & 0x1F)) + 63));
            value >>= 5;
        }
        
        // 3. 마지막 청크
        encoded.append((char) (value + 63));
        
        return encoded.toString();
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
     * LineString을 썸네일용으로 간소화 (좌표 개수 제한)
     * Geoapify URL 길이 제한을 피하기 위해 LTTB 알고리즘으로 다운샘플링
     */
    public static LineString simplifyForThumbnail(LineString lineString) {
        if (lineString == null || lineString.isEmpty()) {
            return lineString;
        }

        Coordinate[] coordinates = lineString.getCoordinates();
        
        // 원본의 약 10% 정도로 축소, 최소 20개, 최대 80개
        int targetPoints = Math.min(80, Math.max(20, coordinates.length / 10));
        
        // 이미 목표 개수 이하면 그대로 사용
        if (coordinates.length <= targetPoints) {
            return lineString;
        }

        // LTTB 알고리즘으로 다운샘플링
        List<Point> points = convertCoordinatesToPointsForThumbnail(List.of(coordinates));
        List<Point> downsampledPoints = LTThreeBuckets.sorted(points, targetPoints);
        
        // Point를 다시 Coordinate로 변환
        List<Coordinate> downsampledCoords = downsampledPoints.stream()
                .map(point -> {
                    int index = (int) point.getX();
                    return coordinates[index];
                })
                .collect(Collectors.toList());
        
        return geometryFactory.createLineString(downsampledCoords.toArray(new Coordinate[0]));
    }

    /**
     * 썸네일용 좌표를 Point로 변환 (인덱스 기반)
     * LTTB 알고리즘에서 사용할 수 있도록 각 좌표에 인덱스를 부여
     */
    private static List<Point> convertCoordinatesToPointsForThumbnail(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.isEmpty()) {
            return new ArrayList<>();
        }

        return IntStream.range(0, coordinates.size())
            .mapToObj(i -> {
                Coordinate coordinate = coordinates.get(i);
                // X축은 인덱스, Y축은 위도를 사용 (시각적 다운샘플링을 위해)
                return new DoublePoint(i, coordinate.y);
            })
            .collect(Collectors.toList());
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

    public static List<Point> convertCoordinatesToPoints(List<Coordinate> coordinates){
        if (coordinates == null || coordinates.isEmpty()) {
            return new ArrayList<>();
        }

        return IntStream.range(0, coordinates.size())
            .mapToObj(i -> {
                Coordinate coordinate = coordinates.get(i);

                double elevation = Double.isNaN(coordinate.getZ()) ? 0.0 : coordinate.getZ();

                return new DoublePoint(i, elevation);
            })
            .collect(Collectors.toList());
    }

    /**
     * 자전거 경로 특성에 맞는 최적 샘플링 크기 계산
     * @param distanceKm 경로 거리 (km)
     * @param originalSize 원본 데이터 포인트 수
     * @return 최적 샘플링 크기
     */
    public static int calculateOptimalSampleSize(Double distanceKm, int originalSize) {
        // 기본값: 모바일 차트에 최적화된 크기
        int baseSampleSize = 150;
        
        // 거리별 적응형 샘플링
        if (distanceKm != null) {
            if (distanceKm <= 5.0) {
                baseSampleSize = 100;
            } else if (distanceKm <= 15.0) {
                baseSampleSize = 150;
            } else if (distanceKm <= 50.0) {
                baseSampleSize = 200;
            } else {
                baseSampleSize = 250;
            }
        }
        
        // 원본 데이터가 너무 적으면 샘플링하지 않음
        return Math.min(baseSampleSize, originalSize);
    }

    /**
     * 좌표 배열을 고도 프로필용으로 다운샘플링
     * @param coordinates 원본 좌표 배열
     * @param distanceKm 경로 거리 (km)
     * @return 다운샘플링된 고도 포인트 목록
     */
    public static List<Point> downsampleElevationProfile(Coordinate[] coordinates, Double distanceKm) {
        if (coordinates == null || coordinates.length == 0) {
            return new ArrayList<>();
        }

        // 좌표를 Point로 변환
        List<Point> elevationPoints = convertCoordinatesToPoints(List.of(coordinates));
        
        // 최적 샘플링 크기 계산
        int targetSampleSize = calculateOptimalSampleSize(distanceKm, elevationPoints.size());
        
        // LTTB 다운샘플링: 입력 데이터가 충분할 때만 적용
        if (elevationPoints.size() > targetSampleSize) {
            return LTThreeBuckets.sorted(elevationPoints, targetSampleSize);
        }
        
        // 입력 데이터가 적으면 원본 데이터를 그대로 사용
        return elevationPoints;
    }
}
