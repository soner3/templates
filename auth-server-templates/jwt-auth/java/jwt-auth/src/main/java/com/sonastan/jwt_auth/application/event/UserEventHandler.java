package com.sonastan.jwt_auth.application.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.sonastan.jwt_auth.domain.event.user.UserCreatedEvent;
import com.sonastan.jwt_auth.domain.model.Profile;
import com.sonastan.jwt_auth.domain.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class UserEventHandler {

    private final ProfileRepository profileRepository;

    @EventListener
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        log.info("Handling UserCreatedEvent for user: {}", event.user().getUsername());
        Profile profile = new Profile(event.user());
        profileRepository.save(profile);
    }

}
