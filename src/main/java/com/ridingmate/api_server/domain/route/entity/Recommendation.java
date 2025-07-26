package com.ridingmate.api_server.domain.route.entity;

import com.ridingmate.api_server.domain.route.enums.RecommendationType;
import com.ridingmate.api_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "recommendations")
public class Recommendation {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "route_id")
    private Route route;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation_type")
    private RecommendationType recommendationType;


    @Builder
    public Recommendation(Route route, RecommendationType recommendationType) {
        this.route = route;
        this.recommendationType = recommendationType;
    }
} 