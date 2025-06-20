package com.sonastan.jwt_auth.infrastructure.security.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.sonastan.jwt_auth.TestcontainersConfiguration;
import com.sonastan.jwt_auth.domain.model.Role;
import com.sonastan.jwt_auth.domain.model.User;
import com.sonastan.jwt_auth.infrastructure.constants.UserRole;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class UserDetailsImplTest {

    @Test
    void test_returns_user_details() {
        User user = new User("username", "email", "UserUser1234!", "firstname",
                "lastname", new Role(UserRole.ROLE_USER));
        UserDetails userDetails = UserDetailsImpl.build(user);
        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(UserDetailsImpl.class);
        assertThat(userDetails.getUsername()).isEqualTo("username");
        assertThat(userDetails.getPassword()).isEqualTo("UserUser1234!");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities()
                .contains(new SimpleGrantedAuthority(UserRole.ROLE_USER.name())))
                .isTrue();
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();

    }
}
