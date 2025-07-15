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
     * 사용자별 특정 관계 타입의 경로 조회 (페이지네이션)
     */
    @Query("""
        SELECT r FROM Route r
        JOIN UserRoute ur ON ur.route = r
        WHERE ur.user = :user
        AND ur.relationType = :relationType
        AND ur.isDelete = false
        """)
    Page<Route> findByUserAndRelationType(@Param("user") User user, 
                                         @Param("relationType") RouteRelationType relationType, 
                                         Pageable pageable);

    /**
     * 사용자별 여러 관계 타입의 경로 조회 (페이지네이션)
     */
    @Query("""
        SELECT r FROM Route r
        JOIN UserRoute ur ON ur.route = r
        WHERE ur.user = :user
        AND ur.relationType IN :relationTypes
        AND ur.isDelete = false
        """)
    Page<Route> findByUserAndRelationTypes(@Param("user") User user, 
                                          @Param("relationTypes") List<RouteRelationType> relationTypes, 
                                          Pageable pageable);

    /**
     * 사용자별 모든 관계 타입의 경로 조회 (페이지네이션)
     */
    @Query("""
        SELECT r FROM Route r
        JOIN UserRoute ur ON ur.route = r
        WHERE ur.user = :user
        AND ur.isDelete = false
        """)
    Page<Route> findByUserWithRelations(@Param("user") User user, Pageable pageable);
}
