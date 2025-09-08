package com.ridingmate.api_server.domain.user.repository;

import com.ridingmate.api_server.domain.user.entity.TerraUser;
import com.ridingmate.api_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TerraUserRepository extends JpaRepository<TerraUser, Long> {

    List<TerraUser> findAllByUserAndIsActiveTrue(User user);

    Optional<TerraUser> findByTerraUserId(UUID terraUserId);
}
