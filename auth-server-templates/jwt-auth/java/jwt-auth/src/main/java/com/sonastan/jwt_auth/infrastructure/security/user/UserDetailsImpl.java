package com.sonastan.jwt_auth.infrastructure.security.user;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sonastan.jwt_auth.domain.model.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@AllArgsConstructor
@Slf4j
public class UserDetailsImpl implements UserDetails {

    private Long userId;

    private String userUuid;

    private String username;

    private String email;

    @JsonIgnore
    private String password;

    private String firstname;

    private String lastname;

    private boolean isEnabled;

    private boolean isCredentialsNonExpired;

    private boolean isAccountNonLocked;

    private boolean isAccountNonExpired;

    private Collection<? extends GrantedAuthority> authorities;

    public static UserDetails build(User user) {
        log.debug("Building UserDetails for user: {}", user.getUsername());
        Collection<? extends GrantedAuthority> authorities = List
                .of(new SimpleGrantedAuthority(user.getRole().getRolename().name()));
        return new UserDetailsImpl(user.getUserId(), user.getUserUuid(), user.getUsername(), user.getEmail(),
                user.getPassword(), user.getFirstname(), user.getLastname(), user.isEnabled(),
                user.isCredentialsNonExpired(), user.isAccountNonLocked(), user.isAccountNonExpired(), authorities);
    }
}
