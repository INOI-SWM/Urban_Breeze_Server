package com.ridingmate.api_server.domain.user.repository;

import com.ridingmate.api_server.domain.user.entity.TerraUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TerraUserRepository extends JpaRepository<TerraUser, Long> {

}
