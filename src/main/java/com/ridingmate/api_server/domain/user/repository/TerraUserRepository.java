package com.ridingmate.api_server.domain.user.repository;

import com.ridingmate.api_server.domain.user.entity.TerraUser;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.infra.terra.TerraProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TerraUserRepository extends JpaRepository<TerraUser, Long> {

    List<TerraUser> findAllByUserAndIsActiveTrueAndDeletedAtIsNull(User user);

    Optional<TerraUser> findByUserAndProviderAndIsActiveTrueAndDeletedAtIsNull(User user, TerraProvider provider);

    Optional<TerraUser> findByTerraUserIdAndIsActiveTrueAndDeletedAtIsNull(UUID terraUserId);

    Optional<TerraUser> findByTerraUserIdAndDeletedAtIsNull(UUID terraUserId);
}
