package com.ridingmate.api_server.domain.user.service;

import com.ridingmate.api_server.domain.user.entity.TerraUser;
import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.domain.user.exception.UserErrorCode;
import com.ridingmate.api_server.domain.user.exception.UserException;
import com.ridingmate.api_server.domain.user.repository.TerraUserRepository;
import com.ridingmate.api_server.domain.user.repository.UserRepository;
import com.ridingmate.api_server.infra.terra.TerraProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TerraUserService {

    private final UserRepository userRepository;
    private final TerraUserRepository terraUserRepository;

    @Transactional
    public void createTerraUser(Long userId, UUID terraUserId, TerraProvider terraProvider){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        TerraUser terraUser = TerraUser.builder()
                .user(user)
                .terraUserId(terraUserId)
                .provider(terraProvider)
                .build();

        terraUserRepository.save(terraUser);
    }
}
