package com.sonastan.jwt_auth.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sonastan.jwt_auth.domain.model.Role;
import com.sonastan.jwt_auth.infrastructure.constants.UserRole;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRolename(UserRole rolename);

    boolean existsByRolename(UserRole rolename);
}
