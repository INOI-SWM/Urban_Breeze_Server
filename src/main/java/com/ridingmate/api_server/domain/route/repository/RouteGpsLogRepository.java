package com.ridingmate.api_server.domain.route.repository;

import com.ridingmate.api_server.domain.route.entity.RouteGpsLog;
import com.ridingmate.api_server.domain.route.enums.WaypointType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteGpsLogRepository extends JpaRepository<RouteGpsLog, Long> {
    List<RouteGpsLog> findByRouteIdOrderByLogTimeAsc(Long routeId);
    
    /**
     * 특정 경로의 모든 GPS 로그 삭제
     * @param routeId 경로 ID
     */
    void deleteByRouteId(Long routeId);
    
    /**
     * 특정 경로의 Waypoint만 조회
     * @param routeId 경로 ID
     * @return Waypoint가 있는 GPS 로그 목록
     */
    @Query("SELECT rgl FROM RouteGpsLog rgl WHERE rgl.route.id = :routeId AND rgl.waypointType IS NOT NULL ORDER BY rgl.logTime ASC")
    List<RouteGpsLog> findWaypointsByRouteId(@Param("routeId") Long routeId);
    
    /**
     * 특정 경로의 일반 GPS 로그만 조회 (Waypoint 제외)
     * @param routeId 경로 ID
     * @return Waypoint가 없는 GPS 로그 목록
     */
    @Query("SELECT rgl FROM RouteGpsLog rgl WHERE rgl.route.id = :routeId AND rgl.waypointType IS NULL ORDER BY rgl.logTime ASC")
    List<RouteGpsLog> findGpsLogsByRouteId(@Param("routeId") Long routeId);
    
    /**
     * 특정 경로의 특정 Waypoint 타입 조회
     * @param routeId 경로 ID
     * @param waypointType Waypoint 타입
     * @return 해당 타입의 Waypoint 목록
     */
    List<RouteGpsLog> findByRouteIdAndWaypointTypeOrderByLogTimeAsc(Long routeId, WaypointType waypointType);
    
    /**
     * 특정 경로의 Waypoint 개수 조회
     * @param routeId 경로 ID
     * @return Waypoint 개수
     */
    @Query("SELECT COUNT(rgl) FROM RouteGpsLog rgl WHERE rgl.route.id = :routeId AND rgl.waypointType IS NOT NULL")
    long countWaypointsByRouteId(@Param("routeId") Long routeId);
}
