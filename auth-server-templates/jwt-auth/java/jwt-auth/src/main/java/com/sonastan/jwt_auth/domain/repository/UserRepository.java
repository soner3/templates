package com.sonastan.jwt_auth.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sonastan.jwt_auth.domain.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByUserUuid(String userUuid);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    int deleteByUserUuid(String userUuid);
}
