package com.ridingmate.api_server.global.util;

import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.route.entity.RouteGpsLog;
import com.ridingmate.api_server.domain.route.enums.WaypointType;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * TCX 파일 생성 유틸리티 클래스
 * Garmin Training Center XML 형식으로 경로 데이터를 생성합니다.
 */
public class TcxGenerator {

    private static final String TCX_HEADER = """
            <?xml version="1.0" encoding="UTF-8"?>
            <TrainingCenterDatabase xmlns="http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2">
                <Folders />
                <Courses>
                    <Course>
                        <Name>%s</Name>
                        <Lap>
                            <TotalTimeSeconds>0</TotalTimeSeconds>
                            <DistanceMeters>%.2f</DistanceMeters>
                            <BeginPosition>
                                <LatitudeDegrees>%.6f</LatitudeDegrees>
                                <LongitudeDegrees>%.6f</LongitudeDegrees>
                            </BeginPosition>
                            <EndPosition>
                                <LatitudeDegrees>%.6f</LatitudeDegrees>
                                <LongitudeDegrees>%.6f</LongitudeDegrees>
                            </EndPosition>
                            <Intensity>Active</Intensity>
                        </Lap>
                        <Track>
            """;

    private static final String TCX_FOOTER = """
                            </Track>
                        </Course>
                    </Courses>
                </TrainingCenterDatabase>
                """;

    private static final String TRACK_POINT_TEMPLATE = """
                                <Trackpoint sectionIndex="0" pointIndex="%d" originalElevation="" isOriginalElevationFromOSM="false">
                                    <Time>%s</Time>
                                    <Position>
                                        <LatitudeDegrees>%.6f</LatitudeDegrees>
                                        <LongitudeDegrees>%.6f</LongitudeDegrees>
                                    </Position>
                                    <AltitudeMeters>%.1f</AltitudeMeters>
                                    <DistanceMeters>%.2f</DistanceMeters>
                                </Trackpoint>
            """;

    private static final String COURSE_POINT_TEMPLATE = """
                                <CoursePoint sectionIndex="0" pointIndex="%d">
                                    <Name>%s</Name>
                                    <Time>%s</Time>
                                    <Position>
                                        <LatitudeDegrees>%.6f</LatitudeDegrees>
                                        <LongitudeDegrees>%.6f</LongitudeDegrees>
                                    </Position>
                                    <AltitudeMeters>%.1f</AltitudeMeters>
                                    <PointType>%s</PointType>
                                    <Notes>%s</Notes>
                                </CoursePoint>
            """;

    /**
     * Route 엔티티와 GPS 로그 목록으로부터 TCX 파일을 생성합니다.
     * RouteGpsLog를 시간순으로 정렬하여 정확한 순서로 TCX를 생성합니다.
     *
     * @param route       Route 엔티티
     * @param routeGpsLogs GPS 로그 목록 (waypoint 정보 포함)
     * @return TCX 파일의 바이트 배열
     * @throws IOException 파일 생성 오류
     */
    public static byte[] generateTcxBytesFromRoute(Route route, List<RouteGpsLog> routeGpsLogs) throws IOException {
        if (route == null || routeGpsLogs == null || routeGpsLogs.isEmpty()) {
            throw new IllegalArgumentException("Route 또는 RouteGpsLogs가 null이거나 비어있습니다.");
        }

        List<RouteGpsLog> sortedGpsLogs = routeGpsLogs.stream()
            .sorted(Comparator.comparing(RouteGpsLog::getLogTime))
            .toList();

        return generateTcxBytesFromGpsLogs(route.getTitle(), sortedGpsLogs);
    }

    /**
     * GPS 로그 목록으로부터 TCX 파일을 생성합니다.
     * 시간순으로 정렬된 GPS 로그를 사용하여 정확한 순서로 TCX를 생성합니다.
     *
     * @param routeTitle Route 제목
     * @param gpsLogs    시간순으로 정렬된 GPS 로그 목록
     * @return TCX 파일의 바이트 배열
     * @throws IOException 파일 생성 오류
     */
    public static byte[] generateTcxBytesFromGpsLogs(String routeTitle, List<RouteGpsLog> gpsLogs) throws IOException {
        if (gpsLogs == null || gpsLogs.isEmpty()) {
            throw new IllegalArgumentException("GPS 로그 목록이 비어있습니다.");
        }

        // TCX 파일을 메모리에서 생성
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(baos)) {
            // TCX 헤더 작성
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
            double totalDistance = calculateTotalDistanceFromGpsLogs(gpsLogs);
            
            // 시작점과 종료점 좌표
            RouteGpsLog firstLog = gpsLogs.get(0);
            RouteGpsLog lastLog = gpsLogs.get(gpsLogs.size() - 1);
            
            writer.printf(TCX_HEADER, routeTitle, totalDistance, 
                firstLog.getLatitude(), firstLog.getLongitude(),
                lastLog.getLatitude(), lastLog.getLongitude());

            // GPS 로그를 순서대로 처리
            double cumulativeDistance = 0.0;
            for (int i = 0; i < gpsLogs.size(); i++) {
                RouteGpsLog gpsLog = gpsLogs.get(i);
                
                // 시간 정보 (실제 GPS 로그 시간 사용)
                String timeStr = gpsLog.getLogTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
                
                // 누적 거리 계산
                if (i > 0) {
                    RouteGpsLog prevLog = gpsLogs.get(i - 1);
                    cumulativeDistance += calculateDistance(
                        prevLog.getLatitude(), prevLog.getLongitude(),
                        gpsLog.getLatitude(), gpsLog.getLongitude()
                    );
                }
                
                writer.printf(TRACK_POINT_TEMPLATE, 
                    i, timeStr, gpsLog.getLatitude(), gpsLog.getLongitude(), 
                    gpsLog.getElevation() != null ? gpsLog.getElevation() : 0.0, 
                    cumulativeDistance);
            }

            // CoursePoint 추가 (Waypoint가 있는 GPS 로그들)
            int coursePointIndex = 0;
            for (int i = 0; i < gpsLogs.size(); i++) {
                RouteGpsLog gpsLog = gpsLogs.get(i);
                if (gpsLog.isWaypoint()) {
                    String timeStr = gpsLog.getLogTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
                    String pointType = mapWaypointTypeToCoursePointType(gpsLog.getWaypointType());
                    String notes = gpsLog.getWaypointDescription() != null ? gpsLog.getWaypointDescription() : "";
                    
                    writer.printf(COURSE_POINT_TEMPLATE,
                        i, // Trackpoint의 실제 인덱스 사용
                        gpsLog.getWaypointTitle() != null ? gpsLog.getWaypointTitle() : "Waypoint",
                        timeStr,
                        gpsLog.getLatitude(), gpsLog.getLongitude(),
                        gpsLog.getElevation() != null ? gpsLog.getElevation() : 0.0,
                        pointType,
                        notes);
                    coursePointIndex++;
                }
            }

            // TCX 푸터 작성
            writer.write(TCX_FOOTER);
        }

        return baos.toByteArray();
    }

    /**
     * TCX 파일 경로를 생성합니다.
     *
     * @param routeId Route ID
     * @return S3에 저장할 TCX 파일 경로
     */
    public static String generateTcxFilePath(Long routeId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("route_%d_%s.tcx", routeId, timestamp);
        return String.format("tcx/route_%d/%s", routeId, fileName);
    }

    /**
     * TCX 파일명을 생성합니다.
     *
     * @param routeTitle Route 제목
     * @return 안전한 TCX 파일명
     */
    public static String generateTcxFileName(String routeTitle) {
        if (routeTitle == null || routeTitle.trim().isEmpty()) {
            return "route.tcx";
        }

        // 파일명에 사용할 수 없는 문자들을 언더스코어로 대체
        String safeFileName = routeTitle
                .replaceAll("[^a-zA-Z0-9가-힣\\s]", "_")  // 특수문자 제거
                .replaceAll("\\s+", "_")                 // 공백을 언더스코어로
                .replaceAll("_{2,}", "_")                // 연속된 언더스코어를 하나로
                .replaceAll("^_|_$", "");                // 앞뒤 언더스코어 제거

        // 빈 문자열이거나 너무 긴 경우 처리
        if (safeFileName.isEmpty() || safeFileName.length() > 100) {
            return "route.tcx";
        }

        return safeFileName + ".tcx";
    }

    /**
     * WaypointType을 CoursePoint의 PointType으로 매핑합니다.
     *
     * @param waypointType WaypointType Enum
     * @return CoursePoint PointType
     */
    private static String mapWaypointTypeToCoursePointType(WaypointType waypointType) {
        if (waypointType == null) {
            return "Generic";
        }
        
        return switch (waypointType) {
            case LEFT -> "Left";
            case RIGHT -> "Right";
            case STRAIGHT -> "Straight";
            case GENERIC, SUMMIT, VALLEY, WATER, FOOD, DANGER, FIRST_AID,
                 CATEGORY_4, CATEGORY_3, CATEGORY_2, CATEGORY_1, HORS_CATEGORY, SPRINT -> "Generic";
        };
    }

    /**
     * LineString의 총 거리를 계산합니다.
     *
     * @param routeLine LineString
     * @return 총 거리 (미터)
     */
    private static double calculateTotalDistance(LineString routeLine) {
        if (routeLine == null || routeLine.getCoordinates().length < 2) {
            return 0.0;
        }

        double totalDistance = 0.0;
        Coordinate[] coordinates = routeLine.getCoordinates();
        
        for (int i = 1; i < coordinates.length; i++) {
            totalDistance += calculateDistance(
                coordinates[i-1].getY(), coordinates[i-1].getX(),
                coordinates[i].getY(), coordinates[i].getX()
            );
        }
        
        return totalDistance;
    }

    /**
     * 좌표 배열의 총 거리를 계산합니다.
     *
     * @param coordinates 좌표 배열
     * @return 총 거리 (미터)
     */
    private static double calculateTotalDistanceFromCoordinates(Coordinate[] coordinates) {
        if (coordinates == null || coordinates.length < 2) {
            return 0.0;
        }

        double totalDistance = 0.0;
        
        for (int i = 1; i < coordinates.length; i++) {
            totalDistance += calculateDistance(
                coordinates[i-1].getY(), coordinates[i-1].getX(),
                coordinates[i].getY(), coordinates[i].getX()
            );
        }
        
        return totalDistance;
    }

    /**
     * GPS 로그 목록의 총 거리를 계산합니다.
     *
     * @param gpsLogs GPS 로그 목록
     * @return 총 거리 (미터)
     */
    private static double calculateTotalDistanceFromGpsLogs(List<RouteGpsLog> gpsLogs) {
        if (gpsLogs == null || gpsLogs.size() < 2) {
            return 0.0;
        }

        double totalDistance = 0.0;
        
        for (int i = 1; i < gpsLogs.size(); i++) {
            RouteGpsLog prev = gpsLogs.get(i-1);
            RouteGpsLog curr = gpsLogs.get(i);
            
            if (prev.getLatitude() != null && prev.getLongitude() != null &&
                curr.getLatitude() != null && curr.getLongitude() != null) {
                totalDistance += calculateDistance(
                    prev.getLatitude(), prev.getLongitude(),
                    curr.getLatitude(), curr.getLongitude()
                );
            }
        }
        
        return totalDistance;
    }

    /**
     * 두 좌표 간의 거리를 계산합니다 (Haversine 공식).
     *
     * @param lat1 첫 번째 점의 위도
     * @param lon1 첫 번째 점의 경도
     * @param lat2 두 번째 점의 위도
     * @param lon2 두 번째 점의 경도
     * @return 거리 (미터)
     */
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371000; // 지구 반지름 (미터)
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }

}
