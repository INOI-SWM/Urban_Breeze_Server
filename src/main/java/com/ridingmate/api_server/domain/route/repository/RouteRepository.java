package com.ridingmate.api_server.domain.route.repository;

import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.route.enums.Difficulty;
import com.ridingmate.api_server.domain.route.enums.RecommendationType;
import com.ridingmate.api_server.domain.route.enums.Region;
import com.ridingmate.api_server.domain.route.enums.RouteRelationType;
import com.ridingmate.api_server.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    @Query("""
        SELECT r
        FROM Route r
        JOIN FETCH r.user
        WHERE r.id = :routeId
        """)
    Optional<Route> findRouteWithUser(@Param("routeId") Long routeId);

    /**
     * 사용자별 특정 관계 타입의 경로 조회 (거리 및 고도 필터링 포함)
     */
    @Query("""
        SELECT r FROM Route r
        JOIN UserRoute ur ON ur.route = r
        WHERE ur.user = :user
        AND ur.relationType = :relationType
        AND ur.isDelete = false
        AND (:minDistance IS NULL OR r.distance >= :minDistance)
        AND (:maxDistance IS NULL OR r.distance <= :maxDistance)
        AND (:minElevationGain IS NULL OR r.elevationGain >= :minElevationGain)
        AND (:maxElevationGain IS NULL OR r.elevationGain <= :maxElevationGain)
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
        AND (:minDistance IS NULL OR r.distance >= :minDistance)
        AND (:maxDistance IS NULL OR r.distance <= :maxDistance)
        AND (:minElevationGain IS NULL OR r.elevationGain >= :minElevationGain)
        AND (:maxElevationGain IS NULL OR r.elevationGain <= :maxElevationGain)
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
        AND (:minDistance IS NULL OR r.distance >= :minDistance)
        AND (:maxDistance IS NULL OR r.distance <= :maxDistance)
        AND (:minElevationGain IS NULL OR r.elevationGain >= :minElevationGain)
        AND (:maxElevationGain IS NULL OR r.elevationGain <= :maxElevationGain)
        """)
    Page<Route> findByUserWithRelationsAndFilters(@Param("user") User user,
                                                  @Param("minDistance") Double minDistance,
                                                  @Param("maxDistance") Double maxDistance,
                                                  @Param("minElevationGain") Double minElevationGain,
                                                  @Param("maxElevationGain") Double maxElevationGain,
                                                  Pageable pageable);

    /**
     * 추천 코스 목록 조회 (필터링 포함)
     */
    @Query("""
        SELECT r FROM Route r
        JOIN r.recommendation rec
        WHERE (:recommendationTypes IS NULL OR rec.recommendationType IN :recommendationTypes)
        AND (:regions IS NULL OR r.region IN :regions)
        AND (:difficulties IS NULL OR r.difficulty IN :difficulties)
        AND (:minDistance IS NULL OR r.distance >= :minDistance)
        AND (:maxDistance IS NULL OR r.distance <= :maxDistance)
        AND (:minElevationGain IS NULL OR r.elevationGain >= :minElevationGain)
        AND (:maxElevationGain IS NULL OR r.elevationGain <= :maxElevationGain)
        """)
    Page<Route> findRecommendationRoutesWithFilters(
            @Param("recommendationTypes") List<RecommendationType> recommendationTypes,
            @Param("regions") List<Region> regions,
            @Param("difficulties") List<Difficulty> difficulties,
            @Param("minDistance") Double minDistance,
            @Param("maxDistance") Double maxDistance,
            @Param("minElevationGain") Double minElevationGain,
            @Param("maxElevationGain") Double maxElevationGain,
            Pageable pageable);
}
