package com.sonastan.jwt_auth.interfaces.rest.dto.user;

public record ResponseUserDto(String userUuid, String username, String email, String firstName, String lastName) {

}
