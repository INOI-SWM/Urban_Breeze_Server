package com.ridingmate.api_server.domain.user.entity;

import com.ridingmate.api_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Apple HealthKit 연동 사용자 정보
 */
@Entity
@Table(name = "apple_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppleUser extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "last_sync_date")
    private LocalDateTime lastSyncDate;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public AppleUser(User user, boolean isActive, 
                     LocalDateTime lastSyncDate) {
        this.user = user;
        this.isActive = isActive;
        this.lastSyncDate = lastSyncDate;
    }

    /**
     * Apple 연동 활성화
     */
    public void activate() {
        this.isActive = true;
        this.lastSyncDate = LocalDateTime.now();
    }

    /**
     * Apple 연동 비활성화
     */
    public void deactivate() {
        this.isActive = false;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 마지막 동기화 시간 업데이트
     */
    public void updateLastSyncDate() {
        this.lastSyncDate = LocalDateTime.now();
    }

}
