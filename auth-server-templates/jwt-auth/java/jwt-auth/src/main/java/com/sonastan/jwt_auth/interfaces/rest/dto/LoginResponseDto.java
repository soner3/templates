package com.sonastan.jwt_auth.interfaces.rest.dto;

public record LoginResponseDto(String accessToken, String refreshToken) {
}
