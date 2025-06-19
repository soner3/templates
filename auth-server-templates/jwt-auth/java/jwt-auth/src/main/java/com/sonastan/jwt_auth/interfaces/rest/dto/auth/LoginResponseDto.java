package com.sonastan.jwt_auth.interfaces.rest.dto.auth;

public record LoginResponseDto(String accessToken, String refreshToken) {
}
