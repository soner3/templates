package com.sonastan.jwt_auth.domain.event.user;

import com.sonastan.jwt_auth.domain.model.User;

public record UserCreatedEvent(User user) {

}
