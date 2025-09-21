package com.ridingmate.api_server.domain.activity.repository;

import com.ridingmate.api_server.domain.activity.dto.projection.GpsLogProjection;
import com.ridingmate.api_server.domain.activity.entity.Activity;
import com.ridingmate.api_server.domain.activity.entity.ActivityGpsLog;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityGpsLogRepository extends JpaRepository<ActivityGpsLog, Long> {
    
    /**
     * 특정 활동의 모든 GPS 로그를 timestamp 순으로 조회하여 Coordinate 리스트로 반환
     * @param activity 조회할 활동
     * @return GPS 좌표 리스트 (longitude, latitude, elevation)
     */
    @Query("""
        SELECT new org.locationtech.jts.geom.Coordinate(
            agl.longitude,
            agl.latitude,
            COALESCE(agl.elevation, 0.0)
        )
        FROM ActivityGpsLog agl
        WHERE agl.activity = :activity
        ORDER BY agl.logTime ASC
        """)
    List<Coordinate> findCoordinatesByActivity(@Param("activity") Activity activity);

    /**
     * 특정 활동의 모든 GPS 로그를 timestamp 순으로 조회
     * @param activity 조회할 활동
     * @return GPS 로그 리스트
     */
    @Query("""
        SELECT agl
        FROM ActivityGpsLog agl
        WHERE agl.activity = :activity 
        ORDER BY agl.logTime ASC
        """)
    List<ActivityGpsLog> findGpsLogsByActivity(@Param("activity") Activity activity);

    /**
     * 특정 활동의 GPS 좌표와 상세 정보를 한 번에 조회 (Projection DTO 사용)
     * @param activity 조회할 활동
     * @return GPS 좌표와 상세 정보가 포함된 Projection 리스트
     */
    @Query("""
        SELECT 
            agl.longitude as longitude,
            agl.latitude as latitude,
            COALESCE(agl.elevation, 0.0) as elevation,
            agl.speed as speed,
            agl.heartRate as heartRate,
            agl.cadence as cadence,
            agl.power as power
        FROM ActivityGpsLog agl 
        WHERE agl.activity = :activity 
        ORDER BY agl.logTime ASC
        """)
    List<GpsLogProjection> findGpsLogProjectionsByActivity(@Param("activity") Activity activity);
}