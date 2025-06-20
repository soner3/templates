package com.sonastan.jwt_auth.application.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.sonastan.jwt_auth.TestcontainersConfiguration;
import com.sonastan.jwt_auth.domain.event.user.UserCreatedEvent;
import com.sonastan.jwt_auth.domain.model.Profile;
import com.sonastan.jwt_auth.domain.model.Role;
import com.sonastan.jwt_auth.domain.model.User;
import com.sonastan.jwt_auth.domain.repository.ProfileRepository;
import com.sonastan.jwt_auth.domain.repository.RoleRepository;
import com.sonastan.jwt_auth.domain.repository.UserRepository;
import com.sonastan.jwt_auth.infrastructure.constants.UserRole;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class UserEventHandlerTest {

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    TestEventListener testEventListener;

    @BeforeEach
    void beforeEach() {
        testEventListener.reset();
    }

    @Test
    void test_user_created_then_profile_is_created() {
        User user = userRepository
                .save(new User("test1", "test1", "null", "null", "null",
                        roleRepository.save(new Role(UserRole.ROLE_USER))));
        eventPublisher.publishEvent(new UserCreatedEvent(user));
        Optional<Profile> maybeProfile = profileRepository.findByUser(user);
        assertThat(maybeProfile.isPresent()).isTrue();
        Profile profile = maybeProfile.get();
        assertThat(profile).isNotNull();
        assertThat(profile.getUser().getUserId()).isEqualTo(user.getUserId());
    }

    @Test
    void test_user_created_event_is_listened() {
        User user = userRepository
                .save(new User("test2", "test2", "null", "null", "null",
                        roleRepository.save(new Role(UserRole.ROLE_USER))));
        eventPublisher.publishEvent(new UserCreatedEvent(user));
        assertThat(testEventListener.events.size()).isEqualTo(1);
        assertThat(testEventListener.events.get(0).user()).isEqualTo(user);
    }

}

@Component
class TestEventListener {

    final List<UserCreatedEvent> events = new ArrayList<>();

    @EventListener
    void onEvent(UserCreatedEvent event) {
        events.add(event);
    }

    void reset() {
        events.clear();
    }
}
