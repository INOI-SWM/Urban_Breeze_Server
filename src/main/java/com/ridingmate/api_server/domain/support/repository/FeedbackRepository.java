package com.ridingmate.api_server.domain.support.repository;

import com.ridingmate.api_server.domain.support.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}