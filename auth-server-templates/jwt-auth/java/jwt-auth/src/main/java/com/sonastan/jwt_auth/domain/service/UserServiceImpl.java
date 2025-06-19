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
    public UserDetails registerUser(String username, String password, String rePassword, String email, String firstname,
            String lastname) {
        log.debug("Registering user with username: {}", username);
        validateUserData(username, email, password, rePassword);
        String encodedPassword = passwordEncoder.encode(password);
        log.info("Password for user '{}' encoded successfully", username);
        Role role = roleRepository.findByRolename(UserRole.ROLE_USER)
                .orElseThrow(() -> {
                    log.error("Role not found: {}", UserRole.ROLE_USER);
                    return new ServerException("Role not found: " + UserRole.ROLE_USER);
                });
        User user = new User(username, email, encodedPassword, firstname, lastname, role);
        publisher.publishEvent(new UserCreatedEvent(user));
        log.info("UserCreatedEvent published for user '{}'", username);
        User persistUser = userRepository.save(user);
        log.info("User '{}' saved successfully with UUID: {}", username, persistUser.getUserUuid());
        return UserDetailsImpl.build(persistUser);
    }

    @Override
    public UserDetails updateUser(String userUuid, String username, String email, String firstname,
            String lastname) {
        log.debug("Updating user with UUID: {}", userUuid);
        User user = userRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> {
                    log.error("User not found with UUID: {}", userUuid);
                    return new IllegalModelArgumentException("User not found with UUID: " + userUuid);
                });
        validateUserData(username, email);
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        User updatedUser = userRepository.save(user);
        log.info("User with UUID '{}' updated successfully", userUuid);
        return UserDetailsImpl.build(updatedUser);
    }

    @Override
    public void deleteUser(String userUuid) {
        log.debug("Deleting user with UUID: {}", userUuid);
        int deleteCount = userRepository.deleteByUserUuid(userUuid);
        if (deleteCount == 0) {
            log.warn("No user found with UUID to delete: {}", userUuid);
            throw new IllegalModelArgumentException("No user found with UUID: " + userUuid);
        }
        log.info("User with UUID '{}' deleted successfully", userUuid);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        log.debug("Loading user by username: {}", username);
        return userRepository.findByUsername(username)
                .map(user -> {
                    log.info("User '{}' loaded successfully", username);
                    return UserDetailsImpl.build(user);
                })
                .orElseThrow(() -> {
                    log.error("User not found with username: {}", username);
                    return new IllegalModelArgumentException("User not found with username: " + username);
                });
    }

    @Override
    public UserDetails loadUserByUuid(String userUuid) {
        log.debug("Loading user by UUID: {}", userUuid);
        return userRepository.findByUserUuid(userUuid)
                .map(user -> {
                    log.info("User with UUID '{}' loaded successfully", userUuid);
                    return UserDetailsImpl.build(user);
                })
                .orElseThrow(() -> {
                    log.error("User not found with UUID: {}", userUuid);
                    return new IllegalModelArgumentException("User not found with UUID: " + userUuid);
                });
    }

    private void validateUserData(String username, String email) {

        if (userRepository.existsByUsername(username)) {
            log.warn("User with username '{}' already exists", username);
            throw new IllegalModelArgumentException("User with username '" + username + "' already exists");
        }

        if (userRepository.existsByEmail(email)) {
            log.warn("User with email '{}' already exists", email);
            throw new IllegalModelArgumentException("User with email '" + email + "' already exists");
        }
        log.debug("Validation passed for username '{}' and email '{}'", username, email);
    }

    private void validateUserData(String username, String email, String password, String rePassword) {

        if (!password.equals(rePassword)) {
            log.warn("Password and confirm password do not match");
            throw new IllegalModelArgumentException("Password and confirm password do not match");
        }

        if (userRepository.existsByUsername(username)) {
            log.warn("User with username '{}' already exists", username);
            throw new IllegalModelArgumentException("User with username '" + username + "' already exists");
        }

        if (userRepository.existsByEmail(email)) {
            log.warn("User with email '{}' already exists", email);
            throw new IllegalModelArgumentException("User with email '" + email + "' already exists");
        }

        if (passwordChecker.check(password).isCompromised()) {
            log.warn("Password '{}' is compromised", password);
            throw new IllegalModelArgumentException(
                    "Password '" + password + "' is compromised. Please choose a different password.");
        }
        log.debug("Validation passed for username '{}', email '{}' and password", username, email);
    }

}
