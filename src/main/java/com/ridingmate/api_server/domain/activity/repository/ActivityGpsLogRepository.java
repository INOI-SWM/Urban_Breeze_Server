package com.ridingmate.api_server.domain.activity.repository;

import com.ridingmate.api_server.domain.activity.entity.ActivityGpsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityGpsLogRepository extends JpaRepository<ActivityGpsLog, Long> {
}