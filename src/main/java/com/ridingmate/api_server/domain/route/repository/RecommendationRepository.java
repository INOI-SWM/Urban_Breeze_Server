package com.ridingmate.api_server.domain.route.repository;

import com.ridingmate.api_server.domain.route.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
}
