package com.sonastan.jwt_auth.interfaces.rest.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserDto(
        @NotBlank(message = "Username is required") @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters") String username,

        @NotBlank(message = "Email is required") @Email(message = "Email should be valid") String email,

        @NotBlank(message = "Firstname is required") @Size(min = 2, max = 50, message = "Firstname must be between 2 and 50 characters") String firstname,

        @NotBlank(message = "Lastname is required") @Size(min = 2, max = 50, message = "Lastname must be between 2 and 50 characters") String lastname) {

}
