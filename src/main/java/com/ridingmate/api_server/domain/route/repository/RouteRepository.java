package com.ridingmate.api_server.domain.route.repository;

import com.ridingmate.api_server.domain.route.entity.Route;
import com.ridingmate.api_server.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    
    /**
     * 사용자별 경로 목록을 페이지네이션과 정렬로 조회
     * @param user 사용자
     * @param pageable 페이지네이션 및 정렬 정보
     * @return 경로 페이지
     */
    Page<Route> findByUser(User user, Pageable pageable);
}
