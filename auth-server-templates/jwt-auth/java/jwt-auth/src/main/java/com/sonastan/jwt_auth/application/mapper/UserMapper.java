package com.sonastan.jwt_auth.application.mapper;

import org.springframework.security.core.userdetails.UserDetails;

import com.sonastan.jwt_auth.infrastructure.security.user.UserDetailsImpl;
import com.sonastan.jwt_auth.interfaces.rest.dto.user.UserResponseDto;

public class UserMapper {

    private UserMapper() {
    }

    public static UserResponseDto mapUserDetailsToUserResponseDto(UserDetails userDetails) {
        if (userDetails instanceof UserDetailsImpl userDetailsImpl) {
            return new UserResponseDto(
                    userDetailsImpl.getUserUuid(),
                    userDetailsImpl.getUsername(),
                    userDetailsImpl.getEmail(),
                    userDetailsImpl.getFirstname(),
                    userDetailsImpl.getLastname());
        }
        throw new IllegalArgumentException("Invalid UserDetails implementation");
    }

}
