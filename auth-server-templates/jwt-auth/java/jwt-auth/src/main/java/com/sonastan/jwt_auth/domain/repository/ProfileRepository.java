package com.sonastan.jwt_auth.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sonastan.jwt_auth.domain.model.Profile;
import com.sonastan.jwt_auth.domain.model.User;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUser(User user);

}
