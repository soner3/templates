package com.sonastan.jwt_auth.domain.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sonastan.jwt_auth.application.service.UserService;
import com.sonastan.jwt_auth.domain.event.user.UserCreatedEvent;
import com.sonastan.jwt_auth.domain.model.Role;
import com.sonastan.jwt_auth.domain.model.User;
import com.sonastan.jwt_auth.domain.repository.RoleRepository;
import com.sonastan.jwt_auth.domain.repository.UserRepository;
import com.sonastan.jwt_auth.infrastructure.constants.UserRole;
import com.sonastan.jwt_auth.infrastructure.exception.IllegalModelArgumentException;
import com.sonastan.jwt_auth.infrastructure.exception.ServerException;
import com.sonastan.jwt_auth.infrastructure.security.user.UserDetailsImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher publisher;
    private final CompromisedPasswordChecker passwordChecker;

    @Override
    public UserDetails registerUser(String username, String password, String email, String firstName, String lastName) {
        log.debug("Registering user with username: {}", username);
        validateUserData(username, email, password);
        String encodedPassword = passwordEncoder.encode(password);
        Role role = roleRepository.findByRolename(UserRole.ROLE_USER)
                .orElseThrow(() -> new ServerException("Role not found: " + UserRole.ROLE_USER));
        User user = new User(username, email, encodedPassword, firstName, lastName, role);
        publisher.publishEvent(new UserCreatedEvent(user));
        User persistUser = userRepository.save(user);
        return UserDetailsImpl.build(persistUser);
    }

    @Override
    public UserDetails updateUser(String userUuid, String username, String email, String firstName,
            String lastName) {
        log.debug("Updating user with UUID: {}", userUuid);
        User user = userRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new IllegalModelArgumentException("User not found with UUID: " + userUuid));
        validateUserData(username, email);
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstname(firstName);
        user.setLastname(lastName);
        User updatedUser = userRepository.save(user);
        return UserDetailsImpl.build(updatedUser);
    }

    @Override
    public void deleteUser(String userUuid) {
        log.debug("Deleting user with UUID: {}", userUuid);
        int deleteCount = userRepository.deleteByUserUuid(userUuid);
        if (deleteCount == 0) {
            log.warn("No user found with UUID to delete: {}", userUuid);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        log.debug("Loading user by username: {}", username);
        return userRepository.findByUsername(username)
                .map(UserDetailsImpl::build)
                .orElseThrow(() -> new IllegalModelArgumentException("User not found with username: " + username));
    }

    @Override
    public UserDetails loadUserByUuid(String userUuid) {
        log.debug("Loading user by UUID: {}", userUuid);
        return userRepository.findByUserUuid(userUuid)
                .map(UserDetailsImpl::build)
                .orElseThrow(() -> new IllegalModelArgumentException("User not found with UUID: " + userUuid));
    }

    private void validateUserData(String username, String email) {

        if (userRepository.existsByUsername(username)) {
            log.error("User with username '{}' already exists", username);
            throw new IllegalModelArgumentException("User with username '" + username + "' already exists");
        }

        if (userRepository.existsByEmail(email)) {
            log.error("User with email '{}' already exists", email);
            throw new IllegalModelArgumentException("User with email '" + email + "' already exists");
        }
    }

    private void validateUserData(String username, String email, String password) {

        if (userRepository.existsByUsername(username)) {
            log.error("User with username '{}' already exists", username);
            throw new IllegalModelArgumentException("User with username '" + username + "' already exists");
        }

        if (userRepository.existsByEmail(email)) {
            log.error("User with email '{}' already exists", email);
            throw new IllegalModelArgumentException("User with email '" + email + "' already exists");
        }

        if (passwordChecker.check(password).isCompromised()) {
            log.error("Password '{}' is compromised", password);
            throw new IllegalModelArgumentException(
                    "Password '" + password + "' is compromised. Please choose a different password.");
        }
    }

}
