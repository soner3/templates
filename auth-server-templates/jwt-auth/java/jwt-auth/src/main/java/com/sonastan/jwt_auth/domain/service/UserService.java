package com.sonastan.jwt_auth.domain.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {
    UserDetails registerUser(String username, String password, String email, String firstName, String lastName);

    UserDetails updateUser(String userUuid, String username, String email, String firstName, String lastName);

    void deleteUser(String userUuid);

    UserDetails loadUserByUsername(String username);

    UserDetails loadUserByUuid(String userUuid);
}
