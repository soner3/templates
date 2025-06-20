package com.sonastan.jwt_auth.infrastructure.security;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.sonastan.jwt_auth.infrastructure.security.user.UserDetailsImpl;

@Service
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                return Optional.ofNullable(jwt.getSubject());
            } else if (authentication.getPrincipal() instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                return Optional.ofNullable(userDetails.getUserUuid());
            } else {
                return Optional.ofNullable(authentication.getName() + "-Unknown");
            }
        }
        return Optional.of("System");
    }

}
