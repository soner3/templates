package com.sonastan.jwt_auth.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sonastan.jwt_auth.domain.model.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}
