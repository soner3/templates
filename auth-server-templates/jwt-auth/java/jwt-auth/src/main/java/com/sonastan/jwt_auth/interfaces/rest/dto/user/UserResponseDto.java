package com.sonastan.jwt_auth.interfaces.rest.dto.user;

public record UserResponseDto(String userUuid, String username, String email, String firstName, String lastName) {

}
