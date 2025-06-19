package com.sonastan.jwt_auth.interfaces.rest.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
                @NotBlank(message = "Username must not be blank") @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters") String username,

                @NotBlank(message = "Password must not be blank") @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters") String password) {
}
