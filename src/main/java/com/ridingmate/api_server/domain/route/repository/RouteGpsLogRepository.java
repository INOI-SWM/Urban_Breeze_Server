package com.ridingmate.api_server.domain.route.repository;

import com.ridingmate.api_server.domain.route.entity.RouteGpsLog;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
