package com.ridingmate.api_server.domain.support.entity;

import com.ridingmate.api_server.domain.support.enums.FeedbackType;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "feedbacks")
public class Feedback extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder
    private Feedback(User user, String content) {
        this.user = user;
        this.content = content;
    }
}