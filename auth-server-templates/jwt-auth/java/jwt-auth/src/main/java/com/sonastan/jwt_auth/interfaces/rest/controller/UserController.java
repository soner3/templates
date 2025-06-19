package com.sonastan.jwt_auth.interfaces.rest.controller;

import org.springframework.http.HttpStatus;
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
import com.sonastan.jwt_auth.interfaces.rest.dto.user.ResponseUserDto;
import com.sonastan.jwt_auth.interfaces.rest.dto.user.UpdateUserDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ResponseUserDto> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        UserDetails userDetails = userService.loadUserByUuid(jwt.getSubject());
        return ResponseEntity.ok(UserMapper.mapUserDetailsToUserResponseDto(userDetails));
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseUserDto> createUser(@RequestBody @Valid CreateUserDto createUserDto) {
        log.debug("Creating user with username: {}", createUserDto.username());
        UserDetails userDetails = userService.registerUser(
                createUserDto.username(),
                createUserDto.password(),
                createUserDto.password(),
                createUserDto.email(),
                createUserDto.firstname(),
                createUserDto.lastname());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.mapUserDetailsToUserResponseDto(userDetails));
    }

    @PutMapping
    public ResponseEntity<ResponseUserDto> updateUser(@AuthenticationPrincipal Jwt jwt,
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
    public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        log.debug("Deleting user");
        userService.deleteUser(jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

}
