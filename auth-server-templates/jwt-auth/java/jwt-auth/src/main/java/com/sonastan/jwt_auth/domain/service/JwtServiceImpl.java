package com.sonastan.jwt_auth.domain.service;

import java.time.Instant;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import com.sonastan.jwt_auth.application.service.JwtService;
import com.sonastan.jwt_auth.application.service.UserService;
import com.sonastan.jwt_auth.infrastructure.constants.JwtType;
import com.sonastan.jwt_auth.infrastructure.security.user.UserDetailsImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtEncoder jwtEncoder;

    private final JwtDecoder jwtDecoder;

    private final UserService userService;

    @Override
    public Jwt validateToken(String token, JwtType type) {
        Jwt jwt = jwtDecoder.decode(token);
        String userUuid = jwt.getSubject();
        log.info("Validating token for user: {}", userUuid);
        if (!jwt.getClaimAsString("type").equals(type.name().toLowerCase())) {
            log.warn("Invalid token type for user: {}. Expected: {}, Found: {}", userUuid, type,
                    jwt.getClaimAsString("type"));
            throw new JwtException("Invalid token type");
        }
        log.info("Token validated successfully for user: {}", userUuid);
        return jwt;
    }

    @Override
    public Jwt generateRefreshToken(UserDetails userDetails) {
        String sub = userDetails.getUsername();
        if (userDetails instanceof UserDetailsImpl) {
            sub = ((UserDetailsImpl) userDetails).getUserUuid();
        }
        log.info("Generating refresh token for user: {}", sub);

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(3600);
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .subject(sub)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .notBefore(now)
                .issuer("http://localhost:8080")
                .claim("type", JwtType.REFRESH.name().toLowerCase())
                .build();

        Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet));
        log.info("Refresh token generated for user: {}", sub);
        return jwt;
    }

    @Override
    public Jwt generateAccessToken(UserDetails userDetails) {
        String sub = userDetails.getUsername();
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        if (userDetails instanceof UserDetailsImpl) {
            sub = ((UserDetailsImpl) userDetails).getUserUuid();
        }
        log.info("Generating access token for user: {}", sub);

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(60 * 5);
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .subject(sub)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .notBefore(now)
                .issuer("http://localhost:8080")
                .claim("scope", authorities.stream().map(GrantedAuthority::getAuthority).toList())
                .claim("type", JwtType.ACCESS.name().toLowerCase())
                .build();

        Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet));
        log.info("Access token generated for user: {}", sub);
        return jwt;
    }

    @Override
    public Jwt refreshAccessToken(Jwt jwt) {
        String userUuid = jwt.getSubject();
        log.info("Refreshing access token for user: {}", userUuid);
        UserDetails user = userService.loadUserByUuid(userUuid);
        Jwt newJwt = generateAccessToken(user);
        log.info("Access token refreshed for user: {}", userUuid);
        return newJwt;
    }

}
