package com.ridingmate.api_server.domain.user.entity;

import com.ridingmate.api_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "profile_image_path", nullable = false)
    private String profileImagePath;
}
