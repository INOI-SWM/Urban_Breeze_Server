package com.ridingmate.api_server.domain.user.entity;

import com.ridingmate.api_server.infra.terra.TerraProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "terra_users", uniqueConstraints = {
        @UniqueConstraint(name = "user_provider", columnNames = {"user_id", "provider"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TerraUser {

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

    @Column(nullable = false)
    private boolean isActive = false;

    @Builder
    public TerraUser(UUID terraUserId, TerraProvider provider, User user) {
        this.terraUserId = terraUserId;
        this.provider = provider;
        this.user = user;
    }
}
