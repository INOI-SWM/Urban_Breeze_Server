package com.ridingmate.api_server.domain.route.service;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GpxParserService {

    private final GeometryFactory geometryFactory = new GeometryFactory();

    public GpxParseResult parseGpxFile(MultipartFile gpxFile) throws IOException {
        log.info("GPX 파일 파싱 시작: {}", gpxFile.getOriginalFilename());

        Path tempFile = Files.createTempFile("gpx_", ".gpx");
        try {
            Files.copy(gpxFile.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            
            GPX gpx = GPX.read(tempFile);
        
            if (gpx.getTracks().isEmpty()) {
                throw new IllegalArgumentException("GPX 파일에 트랙 정보가 없습니다.");
            }

            Track track = gpx.getTracks().get(0);
            List<Coordinate> coordinates = new ArrayList<>();
            LocalDateTime startTime = null;
            LocalDateTime endTime = null;

            for (TrackSegment segment : track.getSegments()) {
                for (WayPoint point : segment.getPoints()) {
                    double lat = point.getLatitude().doubleValue();
                    double lon = point.getLongitude().doubleValue();
                    double elevation = point.getElevation().map(e -> e.doubleValue()).orElse(0.0);
                    
                    coordinates.add(new Coordinate(lon, lat, elevation));
                    
                    if (point.getTime().isPresent()) {
                        LocalDateTime pointTime = point.getTime().get()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime();
                        
                        if (startTime == null || pointTime.isBefore(startTime)) {
                            startTime = pointTime;
                        }
                        if (endTime == null || pointTime.isAfter(endTime)) {
                            endTime = pointTime;
                        }
                    }
                }
            }

            if (coordinates.size() < 2) {
                throw new IllegalArgumentException("GPX 파일에 유효한 좌표가 부족합니다. 최소 2개 이상의 좌표가 필요합니다.");
            }

            // 2D LineString 생성 (Z 차원 제거)
            Coordinate[] coords2D = coordinates.stream()
                    .map(coord -> new Coordinate(coord.getX(), coord.getY())) // Z 차원 제거
                    .toArray(Coordinate[]::new);
            LineString routeLine = geometryFactory.createLineString(coords2D);

            double totalDistance = calculateTotalDistance(coordinates);
            double elevationGain = calculateElevationGain(coordinates);
            Duration duration = calculateDuration(startTime, endTime, coordinates.size());

            // Bounding box 계산
            double minLat = coordinates.stream().mapToDouble(Coordinate::getY).min().orElse(0.0);
            double maxLat = coordinates.stream().mapToDouble(Coordinate::getY).max().orElse(0.0);
            double minLon = coordinates.stream().mapToDouble(Coordinate::getX).min().orElse(0.0);
            double maxLon = coordinates.stream().mapToDouble(Coordinate::getX).max().orElse(0.0);

            log.info("GPX 파일 파싱 완료: 좌표 수={}, 거리={}m, 상승고도={}m, 소요시간={}초", 
                    coordinates.size(), totalDistance, elevationGain, duration.getSeconds());

            return new GpxParseResult(
                    coordinates,
                    routeLine,
                    totalDistance,
                    elevationGain,
                    duration,
                    startTime,
                    endTime,
                    minLat,
                    maxLat,
                    minLon,
                    maxLon
            );
        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                log.warn("임시 파일 삭제 실패: {}", tempFile, e);
            }
        }
    }

    private double calculateTotalDistance(List<Coordinate> coordinates) {
        double totalDistance = 0.0;
        for (int i = 1; i < coordinates.size(); i++) {
            Coordinate prev = coordinates.get(i - 1);
            Coordinate curr = coordinates.get(i);
            totalDistance += calculateHaversineDistance(prev.getY(), prev.getX(), curr.getY(), curr.getX());
        }
        return totalDistance;
    }

    private double calculateElevationGain(List<Coordinate> coordinates) {
        double elevationGain = 0.0;
        for (int i = 1; i < coordinates.size(); i++) {
            double prevElevation = coordinates.get(i - 1).getZ();
            double currElevation = coordinates.get(i).getZ();
            if (currElevation > prevElevation) {
                elevationGain += (currElevation - prevElevation);
            }
        }
        return elevationGain;
    }

    private Duration calculateDuration(LocalDateTime startTime, LocalDateTime endTime, int pointCount) {
        if (startTime != null && endTime != null) {
            return Duration.between(startTime, endTime);
        } else {
            // 시간 정보가 없는 경우, 포인트 수를 기반으로 추정 (1분당 1포인트 가정)
            return Duration.ofMinutes(pointCount);
        }
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구 반지름 (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // 미터 단위로 반환
    }

    public record GpxParseResult(
            List<Coordinate> coordinates,
            LineString routeLine,
            double totalDistance,
            double elevationGain,
            Duration duration,
            LocalDateTime startTime,
            LocalDateTime endTime,
            double minLat,
            double maxLat,
            double minLon,
            double maxLon
    ) {}
}
