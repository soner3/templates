package com.sonastan.jwt_auth.interfaces.rest.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonastan.jwt_auth.TestcontainersConfiguration;
import com.sonastan.jwt_auth.application.service.JwtService;
import com.sonastan.jwt_auth.application.service.UserService;
import com.sonastan.jwt_auth.domain.model.Role;
import com.sonastan.jwt_auth.domain.model.User;
import com.sonastan.jwt_auth.domain.repository.RoleRepository;
import com.sonastan.jwt_auth.domain.repository.UserRepository;
import com.sonastan.jwt_auth.infrastructure.constants.JwtType;
import com.sonastan.jwt_auth.infrastructure.constants.UserRole;
import com.sonastan.jwt_auth.infrastructure.security.user.UserDetailsImpl;
import com.sonastan.jwt_auth.interfaces.rest.dto.auth.LoginRequestDto;
import com.sonastan.jwt_auth.interfaces.rest.dto.auth.LoginResponseDto;
import com.sonastan.jwt_auth.interfaces.rest.dto.auth.RefreshResponseDto;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
public class AuthControllerTest {

        @Autowired
        JwtService jwtService;

        ObjectMapper mapper = new ObjectMapper();

        @Autowired
        MockMvc mvc;

        @Autowired
        UserService userService;

        @Autowired
        RoleRepository roleRepository;

        @Autowired
        UserRepository userRepository;

        @Test
        void test_csrf_is_ok() throws Exception {
                mvc.perform(get("/v1/auth/csrf"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.token").isNotEmpty());
        }

        @Test
        void test_login_status_is_200_if_successfull() throws Exception {
                roleRepository.save(new Role(UserRole.ROLE_USER));
                userService.registerUser("username", "UserUser1234!", "UserUser1234!", "email",
                                "firstname",
                                "lastname");
                String requestBody = mapper.writeValueAsString(new LoginRequestDto("username", "UserUser1234!"));
                MvcResult res = mvc.perform(post("/v1/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andReturn();
                LoginResponseDto responseDto = mapper.readValue(res.getResponse().getContentAsString(),
                                LoginResponseDto.class);
                jwtService.validateToken(responseDto.accessToken(), JwtType.ACCESS);
                jwtService.validateToken(responseDto.refreshToken(), JwtType.REFRESH);
                userRepository.deleteAll();
                roleRepository.deleteAll();
        }

        @Test
        void test_login_status_is_403_without_csrf() throws Exception {
                mvc.perform(post("/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void test_login_status_is_400_if_credentials_are_invalid() throws Exception {
                mvc.perform(post("/v1/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"unknown\",\"password\":\"admin123\"}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void test_refresh_status_is_200_if_successful() throws Exception {
                userRepository.deleteAll();
                roleRepository.deleteAll();
                roleRepository.save(new Role(UserRole.ROLE_USER));
                UserDetails user = userService.registerUser("username", "UserUser1234!", "UserUser1234!", "email",
                                "firstname",
                                "lastname");
                Jwt refreshToken = jwtService.generateRefreshToken(user);

                MvcResult res = mvc.perform(post("/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf().useInvalidToken())
                                .header("Authorization", "Bearer " + refreshToken.getTokenValue()))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andReturn();
                RefreshResponseDto responseDto = mapper.readValue(res.getResponse().getContentAsString(),
                                RefreshResponseDto.class);
                jwtService.validateToken(responseDto.accessToken(), JwtType.ACCESS);
                jwtService.validateToken(refreshToken.getTokenValue(), JwtType.REFRESH);
                userRepository.deleteAll();
                roleRepository.deleteAll();
        }

        @Test
        void test_refresh_status_is_404_if_user_not_found() throws Exception {
                Jwt refreshToken = jwtService.generateRefreshToken(
                                UserDetailsImpl.build(new User("null", "null", "null", "null", "null",
                                                new Role(UserRole.ROLE_USER))));

                mvc.perform(post("/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + refreshToken.getTokenValue()))
                                .andExpect(status().isNotFound());
        }

        @Test
        void test_refresh_status_is_401_if_token_is_missing() throws Exception {
                mvc.perform(post("/v1/auth/refresh")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void test_refresh_status_is_403_if_token_and_csrf_is_missing() throws Exception {
                mvc.perform(post("/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isForbidden());
        }
}
