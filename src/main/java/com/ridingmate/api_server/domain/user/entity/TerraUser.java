package com.ridingmate.api_server.domain.user.entity;

import com.ridingmate.api_server.global.entity.BaseTimeEntity;
import com.ridingmate.api_server.infra.terra.TerraProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "terra_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TerraUser extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "terra_user_id", nullable = false, unique = true)
    private UUID terraUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private TerraProvider provider;

    @Column
    private LocalDateTime lastSyncDate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;  // ← 연동 해제 시간

    @Builder
    public TerraUser(UUID terraUserId, TerraProvider provider, User user, boolean isActive) {
        this.terraUserId = terraUserId;
        this.provider = provider;
        this.user = user;
        this.isActive = isActive;
    }

    public void updateLastSyncDates(){
        lastSyncDate = LocalDateTime.now();
    }

    public void setActive(){
        isActive = true;
    }

    public void setInactive(){
        isActive = false;
    }

    public void delete() {
        deletedAt = LocalDateTime.now();
        isActive = false;
    }
}
