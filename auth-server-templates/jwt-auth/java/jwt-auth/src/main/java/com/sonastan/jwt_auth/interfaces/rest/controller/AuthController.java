package com.sonastan.jwt_auth.interfaces.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sonastan.jwt_auth.application.service.JwtService;
import com.sonastan.jwt_auth.infrastructure.security.user.UserDetailsImpl;
import com.sonastan.jwt_auth.interfaces.rest.dto.auth.LoginRequestDto;
import com.sonastan.jwt_auth.interfaces.rest.dto.auth.LoginResponseDto;
import com.sonastan.jwt_auth.interfaces.rest.dto.auth.RefreshResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        Authentication authReq = UsernamePasswordAuthenticationToken.unauthenticated(loginRequestDto.username(),
                loginRequestDto.password());
        Authentication auth = authenticationManager.authenticate(authReq);
        log.info("User {} logged in successfully", auth.getName());
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Jwt accessToken = jwtService.generateAccessToken(userDetails);
        Jwt refreshToken = jwtService.generateRefreshToken(userDetails);
        return ResponseEntity.ok(new LoginResponseDto(accessToken.getTokenValue(), refreshToken.getTokenValue()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponseDto> refresh(@AuthenticationPrincipal Jwt jwt) {
        Jwt validatedJwt = jwtService.validateToken(jwt.getTokenValue(), "refresh");
        log.info("Refresh token validated for user: {}", validatedJwt.getSubject());
        Jwt accessToken = jwtService.refreshAccessToken(validatedJwt);
        return ResponseEntity.ok(new RefreshResponseDto(accessToken.getTokenValue()));
    }

}
