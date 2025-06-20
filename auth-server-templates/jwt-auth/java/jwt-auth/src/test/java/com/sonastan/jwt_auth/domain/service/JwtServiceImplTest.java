package com.sonastan.jwt_auth.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;

import com.sonastan.jwt_auth.TestcontainersConfiguration;
import com.sonastan.jwt_auth.application.service.JwtService;
import com.sonastan.jwt_auth.domain.model.Role;
import com.sonastan.jwt_auth.domain.model.User;
import com.sonastan.jwt_auth.domain.repository.RoleRepository;
import com.sonastan.jwt_auth.domain.repository.UserRepository;
import com.sonastan.jwt_auth.infrastructure.constants.JwtType;
import com.sonastan.jwt_auth.infrastructure.constants.UserRole;
import com.sonastan.jwt_auth.infrastructure.security.user.UserDetailsImpl;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class JwtServiceImplTest {

    @Autowired
    JwtService jwtService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("test", "test@test.com", "password", "Test", "User",
                roleRepository.save(new Role(UserRole.ROLE_USER))));
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void test_generated_access_token_is_valid() {
        Jwt accessToken = jwtService.generateAccessToken(UserDetailsImpl.build(user));
        assertThat(accessToken).isNotNull();
        assertThat(accessToken.getClaims()).isNotEmpty();
        assertThat(accessToken.getSubject()).isEqualTo(user.getUserUuid());
        assertThat(accessToken.getClaimAsStringList("scope")).containsExactly("ROLE_USER");
        assertThat(accessToken.getClaimAsString("type")).isEqualTo(JwtType.ACCESS.name().toLowerCase());
        jwtService.validateToken(accessToken.getTokenValue(), JwtType.ACCESS);
    }

    @Test
    void test_generated_refresh_token_is_valid() {
        Jwt refreshToken = jwtService.generateRefreshToken(UserDetailsImpl.build(user));
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken.getClaims()).isNotEmpty();
        assertThat(refreshToken.getSubject()).isEqualTo(user.getUserUuid());
        assertThat(refreshToken.getClaimAsString("type")).isEqualTo(JwtType.REFRESH.name().toLowerCase());
    }

    @Test
    void test_refresh_access_token_flow_is_successful() {
        Jwt refreshToken = jwtService.generateRefreshToken(UserDetailsImpl.build(user));
        Jwt accessToken = jwtService.refreshAccessToken(refreshToken);
        assertThat(accessToken).isNotNull();
        assertThat(accessToken.getClaims()).isNotEmpty();
        assertThat(accessToken.getSubject()).isEqualTo(user.getUserUuid());
        assertThat(accessToken.getClaimAsStringList("scope")).containsExactly("ROLE_USER");
        assertThat(accessToken.getClaimAsString("type")).isEqualTo(JwtType.ACCESS.name().toLowerCase());
        jwtService.validateToken(accessToken.getTokenValue(), JwtType.ACCESS);
    }

    @Test
    void test_refresh_and_access_token_are_valid() {
        Jwt refreshToken = jwtService.generateRefreshToken(UserDetailsImpl.build(user));
        Jwt accessToken = jwtService.refreshAccessToken(refreshToken);
        jwtService.validateToken(refreshToken.getTokenValue(), JwtType.REFRESH);
        jwtService.validateToken(accessToken.getTokenValue(), JwtType.ACCESS);
    }
}
