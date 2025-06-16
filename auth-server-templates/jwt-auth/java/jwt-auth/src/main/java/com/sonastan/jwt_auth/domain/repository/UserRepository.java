package com.sonastan.jwt_auth.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sonastan.jwt_auth.domain.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
