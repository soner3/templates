package com.sonastan.jwt_auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sonastan.jwt_auth.domain.model.Role;
import com.sonastan.jwt_auth.domain.model.User;
import com.sonastan.jwt_auth.domain.repository.RoleRepository;
import com.sonastan.jwt_auth.domain.repository.UserRepository;
import com.sonastan.jwt_auth.infrastructure.constants.UserRole;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class JwtAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(JwtAuthApplication.class, args);
	}

	@Bean
	protected CommandLineRunner init(RoleRepository roleRepository, UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		return args -> {

			if (!roleRepository.existsByRolename(UserRole.ROLE_ADMIN)) {
				log.info("Initializing admin role and default admin user");
				Role adminRole = roleRepository.save(new Role(UserRole.ROLE_ADMIN));
				if (!userRepository.existsByUsername("admin")) {
					User adminUser = userRepository.save(
							new User("admin", "admin@example.com", passwordEncoder.encode("admin123"), "Admin", "User",
									adminRole));
					log.info("Admin user created with UUID: {}", adminUser.getUserUuid());

				}
			}

			if (!roleRepository.existsByRolename(UserRole.ROLE_USER)) {
				log.info("Initializing user role...");
				roleRepository.save(new Role(UserRole.ROLE_USER));
			}

		};
	}

}
