package com.sonastan.jwt_auth.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sonastan.jwt_auth.domain.model.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
