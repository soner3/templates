package com.sonastan.jwt_auth.interfaces.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sonastan.jwt_auth.application.mapper.UserMapper;
import com.sonastan.jwt_auth.application.service.UserService;
import com.sonastan.jwt_auth.interfaces.rest.dto.user.CreateUserDto;
import com.sonastan.jwt_auth.interfaces.rest.dto.user.UpdateUserDto;
import com.sonastan.jwt_auth.interfaces.rest.dto.user.UserResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/user")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "User", description = "User management endpoints")
public class UserController {

        private final UserService userService;

        @GetMapping
        @Operation(summary = "Get current user", description = "Returns the currently authenticated user.", security = @SecurityRequirement(name = "jwt"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Current user details", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
        public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
                UserDetails userDetails = userService.loadUserByUuid(jwt.getSubject());
                return ResponseEntity.ok(UserMapper.mapUserDetailsToUserResponseDto(userDetails));
        }

        @PostMapping("/create")
        @Operation(summary = "Create a new user", description = "Registers a new user account.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "User created successfully", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
        public ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid CreateUserDto createUserDto) {
                log.debug("Creating user with username: {}", createUserDto.username());
                UserDetails userDetails = userService.registerUser(
                                createUserDto.username(),
                                createUserDto.password(),
                                createUserDto.rePassword(),
                                createUserDto.email(),
                                createUserDto.firstname(),
                                createUserDto.lastname());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(UserMapper.mapUserDetailsToUserResponseDto(userDetails));
        }

        @PutMapping
        @Operation(summary = "Update current user", description = "Updates the details of the currently authenticated user.", security = @SecurityRequirement(name = "jwt"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User updated successfully", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
        public ResponseEntity<UserResponseDto> updateUser(@AuthenticationPrincipal Jwt jwt,
                        @RequestBody @Valid UpdateUserDto updateUserDto) {
                log.debug("Updating user");
                UserDetails userDetails = userService.updateUser(jwt.getSubject(),
                                updateUserDto.username(),
                                updateUserDto.email(),
                                updateUserDto.firstname(),
                                updateUserDto.lastname());
                return ResponseEntity.ok(UserMapper.mapUserDetailsToUserResponseDto(userDetails));
        }

        @DeleteMapping
        @Operation(summary = "Delete current user", description = "Deletes the currently authenticated user.", security = @SecurityRequirement(name = "jwt"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "User deleted successfully", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
        public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal Jwt jwt) {
                log.debug("Deleting user");
                userService.deleteUser(jwt.getSubject());
                return ResponseEntity.noContent().build();
        }

}
