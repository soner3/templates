package com.sonastan.jwt_auth.infrastructure.security.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.sonastan.jwt_auth.TestcontainersConfiguration;
import com.sonastan.jwt_auth.domain.model.Role;
import com.sonastan.jwt_auth.domain.model.User;
import com.sonastan.jwt_auth.domain.repository.RoleRepository;
import com.sonastan.jwt_auth.domain.repository.UserRepository;
import com.sonastan.jwt_auth.infrastructure.constants.UserRole;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class UserDetailsServiceImplTest {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Test
    void test_load_user_by_username_successfull() {
        userRepository.save(new User("test", "test@test.com", "password", "Test", "User",
                roleRepository.save(new Role(UserRole.ROLE_USER))));
        UserDetails user = userDetailsService.loadUserByUsername("test");
        assertThat(user).isNotNull();
        assertThat(user).isInstanceOf(UserDetailsImpl.class);
        assertThat(user.getUsername()).isEqualTo("test");

        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void test_throws_usernamenotfoundexception_if_user_not_found() {
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("unknown"));
    }

}
