package com.sonastan.jwt_auth.application.mapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import com.sonastan.jwt_auth.domain.model.Role;
import com.sonastan.jwt_auth.domain.model.User;
import com.sonastan.jwt_auth.infrastructure.constants.UserRole;
import com.sonastan.jwt_auth.infrastructure.security.user.UserDetailsImpl;
import com.sonastan.jwt_auth.interfaces.rest.dto.user.UserResponseDto;

public class UserMapperTest {

    @Test
    void test_parameter_is_userdetailsimpl_returns_userresponsedto() {
        UserDetails userDetails = UserDetailsImpl
                .build(new User("user", "null", "null", "null", "null", new Role(UserRole.ROLE_USER)));

        UserResponseDto resDto = UserMapper.mapUserDetailsToUserResponseDto(userDetails);
        Assertions.assertThat(resDto).isInstanceOf(UserResponseDto.class);
        Assertions.assertThat(resDto.username()).isEqualTo("user");
    }

    @Test
    void test_parameter_is_not_userdetailsimpl_throws_illegalargumentexception() {
        Assertions.assertThatThrownBy(() -> UserMapper.mapUserDetailsToUserResponseDto(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
