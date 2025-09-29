package com.ridingmate.api_server.domain.activity.entity;

import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 API 사용량 추적 엔티티
 */
@Entity
@Table(name = "user_api_usage", 
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "year", "month"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserApiUsage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "month", nullable = false)
    private int month;

    @Column(name = "activity_sync_count", nullable = false)
    private int activitySyncCount = 0;

    @Builder
    public UserApiUsage(User user, Integer year, Integer month, int activitySyncCount) {
        this.user = user;
        this.year = year;
        this.month = month;
        this.activitySyncCount = activitySyncCount;
    }

    /**
     * 활동 동기화 횟수 증가
     */
    public void incrementActivitySync() {
        this.activitySyncCount++;
    }
}
