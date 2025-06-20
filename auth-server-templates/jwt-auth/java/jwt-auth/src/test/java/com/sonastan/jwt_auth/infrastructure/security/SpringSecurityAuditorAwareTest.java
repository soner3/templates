package com.sonastan.jwt_auth.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

import com.sonastan.jwt_auth.application.service.JwtService;
import com.sonastan.jwt_auth.application.service.UserService;
import com.sonastan.jwt_auth.domain.model.Role;
import com.sonastan.jwt_auth.domain.repository.RoleRepository;
import com.sonastan.jwt_auth.domain.repository.UserRepository;
import com.sonastan.jwt_auth.infrastructure.constants.UserRole;
import com.sonastan.jwt_auth.infrastructure.security.user.UserDetailsImpl;

@SpringBootTest
public class SpringSecurityAuditorAwareTest {

    @Autowired
    SpringSecurityAuditorAware auditorAware;

    @Autowired
    JwtService jwtService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserService userService;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void test_current_auditor_is_system_if_not_authenticated() {
        assertThat(auditorAware.getCurrentAuditor().get()).isEqualTo("System");
    }

    @Test
    void test_current_auditor_is_sub_if_instance_of_jwt() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        roleRepository.save(new Role(UserRole.ROLE_USER));
        userService.registerUser("username", "UserUser1234!", "UserUser1234!", "email", "null", "null");

        UsernamePasswordAuthenticationToken unauthenticated = UsernamePasswordAuthenticationToken
                .unauthenticated("username", "UserUser1234!");
        Authentication authenticate = authenticationManager.authenticate(unauthenticated);
        SecurityContextHolder.getContext().setAuthentication(authenticate);

        Jwt token = jwtService.generateAccessToken((UserDetails) authenticate.getPrincipal());

        assertThat(auditorAware.getCurrentAuditor().get()).isEqualTo(token.getSubject());

        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void test_current_auditor_is_user_uuid_if_instance_of_userdetailsimpl() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        roleRepository.save(new Role(UserRole.ROLE_USER));
        userService.registerUser("username", "UserUser1234!", "UserUser1234!", "email", "null", "null");

        UsernamePasswordAuthenticationToken unauthenticated = UsernamePasswordAuthenticationToken
                .unauthenticated("username", "UserUser1234!");
        Authentication authenticate = authenticationManager.authenticate(unauthenticated);
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        UserDetailsImpl userDetails = (UserDetailsImpl) authenticate.getPrincipal();

        assertThat(auditorAware.getCurrentAuditor().get()).isEqualTo(userDetails.getUserUuid());

        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void test_current_auditor_is_unknown_if_pricipal_is_of_unknown_type() {
        UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken
                .authenticated("username", null, null);
        SecurityContextHolder.getContext().setAuthentication(token);
        assertThat(auditorAware.getCurrentAuditor().get()).isEqualTo("username-Unknown");
    }

}
