package com.ridingmate.api_server.domain.user.repository;

import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.global.security.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 소셜 provider와 social ID로 사용자 조회
     */
    Optional<User> findBySocialProviderAndSocialId(SocialProvider socialProvider, String socialId);

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 소셜 provider와 social ID로 사용자 존재 여부 확인
     */
    boolean existsBySocialProviderAndSocialId(SocialProvider socialProvider, String socialId);

    /**
     * 이메일로 사용자 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 소셜 provider와 social ID로 사용자 조회 (JPQL)
     */
    @Query("SELECT u FROM User u WHERE u.socialProvider = :provider AND u.socialId = :socialId")
    Optional<User> findUserBySocialLogin(@Param("provider") SocialProvider provider, 
                                       @Param("socialId") String socialId);
}
