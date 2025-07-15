package com.ridingmate.api_server.domain.route.repository;

import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.route.enums.RouteRelationType;
import com.ridingmate.api_server.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    /**
     * 사용자별 특정 관계 타입의 경로 조회 (거리 및 고도 필터링 포함)
     */
    @Query("""
        SELECT r FROM Route r
        JOIN UserRoute ur ON ur.route = r
        WHERE ur.user = :user
        AND ur.relationType = :relationType
        AND ur.isDelete = false
        AND (:minDistance IS NULL OR r.totalDistance >= :minDistance)
        AND (:maxDistance IS NULL OR r.totalDistance <= :maxDistance)
        AND (:minElevationGain IS NULL OR r.totalElevationGain >= :minElevationGain)
        AND (:maxElevationGain IS NULL OR r.totalElevationGain <= :maxElevationGain)
        """)
    Page<Route> findByUserAndRelationTypeWithFilters(@Param("user") User user,
                                                     @Param("relationType") RouteRelationType relationType,
                                                     @Param("minDistance") Double minDistance,
                                                     @Param("maxDistance") Double maxDistance,
                                                     @Param("minElevationGain") Double minElevationGain,
                                                     @Param("maxElevationGain") Double maxElevationGain,
                                                     Pageable pageable);

    /**
     * 사용자별 여러 관계 타입의 경로 조회 (거리 및 고도 필터링 포함)
     */
    @Query("""
        SELECT r FROM Route r
        JOIN UserRoute ur ON ur.route = r
        WHERE ur.user = :user
        AND ur.relationType IN :relationTypes
        AND ur.isDelete = false
        AND (:minDistance IS NULL OR r.totalDistance >= :minDistance)
        AND (:maxDistance IS NULL OR r.totalDistance <= :maxDistance)
        AND (:minElevationGain IS NULL OR r.totalElevationGain >= :minElevationGain)
        AND (:maxElevationGain IS NULL OR r.totalElevationGain <= :maxElevationGain)
        """)
    Page<Route> findByUserAndRelationTypesWithFilters(@Param("user") User user,
                                                      @Param("relationTypes") List<RouteRelationType> relationTypes,
                                                      @Param("minDistance") Double minDistance,
                                                      @Param("maxDistance") Double maxDistance,
                                                      @Param("minElevationGain") Double minElevationGain,
                                                      @Param("maxElevationGain") Double maxElevationGain,
                                                      Pageable pageable);

    /**
     * 사용자별 모든 관계 타입의 경로 조회 (거리 및 고도 필터링 포함)
     */
    @Query("""
        SELECT r FROM Route r
        JOIN UserRoute ur ON ur.route = r
        WHERE ur.user = :user
        AND ur.isDelete = false
        AND (:minDistance IS NULL OR r.totalDistance >= :minDistance)
        AND (:maxDistance IS NULL OR r.totalDistance <= :maxDistance)
        AND (:minElevationGain IS NULL OR r.totalElevationGain >= :minElevationGain)
        AND (:maxElevationGain IS NULL OR r.totalElevationGain <= :maxElevationGain)
        """)
    Page<Route> findByUserWithRelationsAndFilters(@Param("user") User user,
                                                  @Param("minDistance") Double minDistance,
                                                  @Param("maxDistance") Double maxDistance,
                                                  @Param("minElevationGain") Double minElevationGain,
                                                  @Param("maxElevationGain") Double maxElevationGain,
                                                  Pageable pageable);
}
