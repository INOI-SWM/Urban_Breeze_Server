package com.ridingmate.api_server.domain.auth.security;

import com.ridingmate.api_server.domain.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Spring Security가 Principal 객체로 사용할 커스텀 클래스.
 * 인증된 사용자의 id, uuid 등 추가 정보를 담는다.
 */
public record AuthUser(
    Long id,
    UUID uuid,
    String username
) implements UserDetails {

    public static AuthUser from(User user) {
        return new AuthUser(
                user.getId(),
                user.getUuid(),
                user.getEmail()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
