package com.ridingmate.api_server.domain.route.entity;

import com.ridingmate.api_server.domain.route.enums.RouteRelationType;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_routes")
public class UserRoute extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false)
    private RouteRelationType relationType;

    @Column(name = "last_viewed_at")
    private LocalDateTime lastViewedAt;

    @Column(name = "is_delete", nullable = false)
    private Boolean isDelete = false;

    @Column(name = "shared_by_user_id")
    private Long sharedByUserId;

    @Builder
    private UserRoute(User user, Route route, RouteRelationType relationType, 
                      LocalDateTime lastViewedAt, Long sharedByUserId) {
        this.user = user;
        this.route = route;
        this.relationType = relationType;
        this.lastViewedAt = lastViewedAt;
        this.sharedByUserId = sharedByUserId;
        this.isDelete = false;
    }

    public void updateLastViewedAt() {
        this.lastViewedAt = LocalDateTime.now();
    }

    public void markAsDeleted() {
        this.isDelete = true;
    }

    public void restore() {
        this.isDelete = false;
    }
} 