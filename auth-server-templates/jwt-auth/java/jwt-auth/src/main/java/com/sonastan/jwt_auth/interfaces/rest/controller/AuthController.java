package com.sonastan.jwt_auth.interfaces.rest.controller;

import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sonastan.jwt_auth.application.service.JwtService;
import com.sonastan.jwt_auth.infrastructure.constants.JwtType;
import com.sonastan.jwt_auth.infrastructure.security.user.UserDetailsImpl;
import com.sonastan.jwt_auth.interfaces.rest.dto.auth.LoginRequestDto;
import com.sonastan.jwt_auth.interfaces.rest.dto.auth.LoginResponseDto;
import com.sonastan.jwt_auth.interfaces.rest.dto.auth.RefreshResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and token management")
public class AuthController {

        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;

        @Operation(summary = "Authenticate user and return JWT tokens", description = "Authenticates a user with username and password and returns JWT access and refresh tokens.", responses = {
                        @ApiResponse(responseCode = "200", description = "Login successful. Returns JWT access and refresh tokens.", content = @Content(schema = @Schema(implementation = LoginResponseDto.class, title = "LoginResponseDto", description = "Response containing JWT access and refresh tokens."))),
                        @ApiResponse(responseCode = "400", description = "Invalid request. The request body is malformed or missing required fields.", content = @Content(schema = @Schema(implementation = ProblemDetail.class, title = "ProblemDetail", description = "Details about the validation or request error."))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized. The credentials are invalid.", content = @Content(schema = @Schema(title = "Empty", description = "No content returned for unauthorized requests."))),
        })
        @PostMapping("/login")
        public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
                Authentication authReq = UsernamePasswordAuthenticationToken.unauthenticated(loginRequestDto.username(),
                                loginRequestDto.password());
                Authentication auth = authenticationManager.authenticate(authReq);
                log.info("User {} logged in successfully", auth.getName());
                UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
                Jwt accessToken = jwtService.generateAccessToken(userDetails);
                Jwt refreshToken = jwtService.generateRefreshToken(userDetails);
                return ResponseEntity
                                .ok(new LoginResponseDto(accessToken.getTokenValue(), refreshToken.getTokenValue()));
        }

        @Operation(summary = "Refresh access token using a valid refresh token", description = "Refreshes the JWT access token using a valid refresh token. Requires a valid refresh token.", responses = {
                        @ApiResponse(responseCode = "200", description = "Token refreshed. Returns a new JWT access token.", content = @Content(schema = @Schema(implementation = RefreshResponseDto.class, title = "RefreshResponseDto", description = "Response containing the new JWT access token."))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized. The refresh token is missing or invalid.", content = @Content(schema = @Schema(title = "Empty", description = "No content returned for unauthorized requests."))),
                        @ApiResponse(responseCode = "404", description = "User not found.", content = @Content(schema = @Schema(implementation = ProblemDetail.class, title = "ProblemDetail", description = "Details about the user not found error."))),
        }, security = {
                        @SecurityRequirement(name = "jwt"),
        })
        @PostMapping("/refresh")
        public ResponseEntity<RefreshResponseDto> refresh(@AuthenticationPrincipal Jwt jwt) {
                Jwt validatedJwt = jwtService.validateToken(jwt.getTokenValue(), JwtType.REFRESH);
                log.info("Refresh token validated for user: {}", validatedJwt.getSubject());
                Jwt accessToken = jwtService.refreshAccessToken(validatedJwt);
                return ResponseEntity.ok(new RefreshResponseDto(accessToken.getTokenValue()));
        }

        @Operation(summary = "Get CSRF token", description = "Returns the CSRF token for the current session. Useful for clients to include in subsequent requests.", responses = {
                        @ApiResponse(responseCode = "200", description = "CSRF token returned successfully.", content = @Content(schema = @Schema(implementation = CsrfToken.class))),
        })
        @GetMapping("/csrf")
        public ResponseEntity<CsrfToken> csrf(CsrfToken csrfToken) {
                log.info("CSRF token requested for session: {}", csrfToken.getToken());
                return ResponseEntity.ok(csrfToken);
        }
}
