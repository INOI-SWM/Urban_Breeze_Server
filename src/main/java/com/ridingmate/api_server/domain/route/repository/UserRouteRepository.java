package com.ridingmate.api_server.domain.route.repository;

import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.route.entity.UserRoute;
import com.ridingmate.api_server.domain.route.enums.RouteRelationType;
import com.ridingmate.api_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRouteRepository extends JpaRepository<UserRoute, Long> {

    /**
     * 특정 사용자와 경로 간의 특정 관계 타입 조회 (활성 관계만)
     */
    Optional<UserRoute> findByUserAndRouteAndRelationTypeAndIsDeleteFalse(User user, Route route, RouteRelationType relationType);

    /**
     * 특정 사용자의 모든 활성 경로 관계 조회
     */
    List<UserRoute> findByUserAndIsDeleteFalse(User user);
} 