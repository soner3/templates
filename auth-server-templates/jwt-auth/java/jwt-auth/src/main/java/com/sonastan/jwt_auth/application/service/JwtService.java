package com.sonastan.jwt_auth.application.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

import com.sonastan.jwt_auth.infrastructure.constants.JwtType;

public interface JwtService {

    Jwt generateAccessToken(UserDetails userDetails);

    Jwt refreshAccessToken(Jwt jwt);

    Jwt generateRefreshToken(UserDetails userDetails);

    Jwt validateToken(String token, JwtType type);

}
