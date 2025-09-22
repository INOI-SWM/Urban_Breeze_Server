package com.ridingmate.api_server.global.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * GPX 파일 생성 유틸리티 클래스
 */
public class GpxGenerator {

    private static final String GPX_HEADER = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gpx version="1.1" creator="RidingMate" xmlns="http://www.topografix.com/GPX/1/1">
                <metadata>
                    <name>%s</name>
                    <desc>%s</desc>
                    <time>%s</time>
                </metadata>
                <trk>
                    <name>%s</name>
                    <desc>%s</desc>
                    <trkseg>
            """;

    private static final String GPX_FOOTER = """
                    </trkseg>
                </trk>
            </gpx>
            """;

    private static final String TRACK_POINT_TEMPLATE = """
                        <trkpt lat="%.6f" lon="%.6f">
                            <ele>%.2f</ele>
                            <time>%s</time>
                        </trkpt>
            """;

    /**
     * 좌표 리스트로부터 GPX 파일을 생성합니다.
     *
     * @param coordinates 좌표 리스트
     * @param routeName   경로 이름
     * @param filePath    저장할 파일 경로
     * @throws IOException 파일 쓰기 오류
     */
    public static void generateGpxFile(List<Coordinate> coordinates, String routeName, 
                                     String filePath) throws IOException {
        if (coordinates == null || coordinates.isEmpty()) {
            throw new IllegalArgumentException("좌표 리스트가 비어있습니다.");
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            // GPX 헤더 작성
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
            writer.write(String.format(GPX_HEADER, routeName, "", currentTime, routeName, ""));

            // 트랙 포인트 작성
            for (int i = 0; i < coordinates.size(); i++) {
                Coordinate coord = coordinates.get(i);
                double elevation = Double.isNaN(coord.getZ()) ? 0.0 : coord.getZ();
                
                // 시간은 시작 시간부터 1초씩 증가
                LocalDateTime pointTime = LocalDateTime.now().plusSeconds(i);
                String timeStr = pointTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
                
                writer.write(String.format(TRACK_POINT_TEMPLATE, 
                    coord.getY(), coord.getX(), elevation, timeStr));
            }

            // GPX 푸터 작성
            writer.write(GPX_FOOTER);
        }
    }

    /**
     * Route 정보와 LineString으로부터 GPX 파일을 생성하고 바이트 배열로 반환합니다.
     *
     * @param routeId     Route ID
     * @param routeTitle  Route 제목
     * @param routeLine   Route의 LineString
     * @return GPX 파일의 바이트 배열
     * @throws IOException 파일 생성 오류
     */
    public static byte[] generateGpxBytes(Long routeId, String routeTitle, LineString routeLine) throws IOException {
        // GPX 파일을 메모리에서 생성
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(baos)) {
            // GPX 헤더 작성
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
            writer.printf("""
                <?xml version="1.0" encoding="UTF-8"?>
                <gpx version="1.1" creator="RidingMate" xmlns="http://www.topografix.com/GPX/1/1">
                    <metadata>
                        <name>%s</name>
                        <desc></desc>
                        <time>%s</time>
                    </metadata>
                    <trk>
                        <name>%s</name>
                        <desc></desc>
                        <trkseg>
                """, routeTitle, currentTime, routeTitle);

            // 트랙 포인트 작성
            Coordinate[] coordinates = routeLine.getCoordinates();
            for (int i = 0; i < coordinates.length; i++) {
                Coordinate coord = coordinates[i];
                double elevation = Double.isNaN(coord.getZ()) ? 0.0 : coord.getZ();
                
                // 시간은 시작 시간부터 1초씩 증가
                LocalDateTime pointTime = LocalDateTime.now().plusSeconds(i);
                String timeStr = pointTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
                
                writer.printf("""
                        <trkpt lat="%.6f" lon="%.6f">
                            <ele>%.2f</ele>
                            <time>%s</time>
                        </trkpt>
                """, coord.getY(), coord.getX(), elevation, timeStr);
            }

            // GPX 푸터 작성
            writer.write("""
                    </trkseg>
                </trk>
            </gpx>
            """);
        }

        return baos.toByteArray();
    }

    /**
     * GPX 파일 경로를 생성합니다.
     *
     * @param routeId Route ID
     * @return S3에 저장할 GPX 파일 경로
     */
    public static String generateGpxFilePath(Long routeId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("route_%d_%s.gpx", routeId, timestamp);
        return String.format("gpx/route_%d/%s", routeId, fileName);
    }
}
