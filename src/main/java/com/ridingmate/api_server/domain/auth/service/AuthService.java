package com.ridingmate.api_server.domain.auth.service;

import com.ridingmate.api_server.domain.user.entity.User;
import com.ridingmate.api_server.global.security.dto.TokenInfo;
import com.ridingmate.api_server.global.security.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;

} 