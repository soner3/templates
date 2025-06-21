package com.sonastan.jwt_auth.interfaces.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.sonastan.jwt_auth.TestcontainersConfiguration;
import com.sonastan.jwt_auth.application.service.JwtService;
import com.sonastan.jwt_auth.application.service.UserService;
import com.sonastan.jwt_auth.domain.model.Role;
import com.sonastan.jwt_auth.domain.repository.RoleRepository;
import com.sonastan.jwt_auth.domain.repository.UserRepository;
import com.sonastan.jwt_auth.infrastructure.constants.UserRole;
import com.sonastan.jwt_auth.infrastructure.exception.NotFoundException;
import com.sonastan.jwt_auth.interfaces.rest.dto.user.CreateUserDto;
import com.sonastan.jwt_auth.interfaces.rest.dto.user.UpdateUserDto;
import com.sonastan.jwt_auth.interfaces.rest.dto.user.UserResponseDto;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
public class UserControllerTest {

        @Autowired
        MockMvc mvc;

        ObjectMapper mapper = new ObjectMapper();

        @Autowired
        UserService userService;

        @Autowired
        UserRepository userRepository;

        @Autowired
        RoleRepository roleRepository;

        @Autowired
        JwtService jwtService;

        @BeforeEach
        void setUp() {
                userRepository.deleteAll();
                roleRepository.deleteAll();
                roleRepository.save(new Role(UserRole.ROLE_USER));

        }

        @AfterEach
        void tearDown() {
                userRepository.deleteAll();
                roleRepository.deleteAll();
        }

        @Test
        void test_create_user_status_is_200_if_successfull() throws Exception {
                CreateUserDto createUserDto = new CreateUserDto("username", "user@test.com", "firstname", "lastname",
                                "UserUser1234!", "UserUser1234!");
                MvcResult res = mvc.perform(post("/v1/user/create")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(createUserDto)))
                                .andExpect(status().isCreated())
                                .andReturn();

                UserResponseDto userResponse = mapper.readValue(res.getResponse().getContentAsString(),
                                UserResponseDto.class);
                UserDetails userByUuid = userService.loadUserByUuid(userResponse.userUuid());
                assertThat(userByUuid).isNotNull();
                assertThat(userByUuid.getUsername()).isEqualTo(createUserDto.username());

        }

        @Test
        void test_create_user_status_is_400_if_values_are_invalid() throws Exception {
                CreateUserDto createUserDto = new CreateUserDto("username", "user", "firstname", "lastname",
                                "User", "UserUser1234!");
                MvcResult res = mvc.perform(post("/v1/user/create")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(createUserDto)))
                                .andExpect(status().isBadRequest())
                                .andReturn();

                assertThrows(UnrecognizedPropertyException.class,
                                () -> mapper.readValue(res.getResponse().getContentAsString(), ProblemDetail.class));
        }

        @Test
        void test_create_user_status_is_403_if_csrf_is_missing() throws Exception {
                mvc.perform(post("/v1/user/create")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isForbidden());
        }

        @Test
        void test_delete_current_user_status_is_204_if_successful() throws Exception {
                UserDetails registerUser = userService.registerUser("username", "UserUser1234!", "UserUser1234!",
                                "user@test.com",
                                "firstname", "lastname");
                Jwt jwt = jwtService.generateAccessToken(registerUser);
                mvc.perform(delete("/v1/user")
                                .header("Authorization", "Bearer " + jwt.getTokenValue()))
                                .andExpect(status().isNoContent());
                assertThrows(NotFoundException.class,
                                () -> userService.loadUserByUuid(registerUser.getUsername()));
        }

        @Test
        void test_delete_current_user_status_is_404_if_user_not_found() throws Exception {
                UserDetails registerUser = userService.registerUser("username", "UserUser1234!", "UserUser1234!",
                                "user@test.com",
                                "firstname", "lastname");
                Jwt jwt = jwtService.generateAccessToken(registerUser);
                userRepository.deleteAll();
                mvc.perform(delete("/v1/user")
                                .header("Authorization", "Bearer " + jwt.getTokenValue()))
                                .andExpect(status().isNotFound());
        }

        @Test
        void test_get_current_user_status_is_200_if_successful() throws Exception {
                UserDetails registerUser = userService.registerUser("username", "UserUser1234!", "UserUser1234!",
                                "user@test.com",
                                "firstname", "lastname");
                Jwt jwt = jwtService.generateAccessToken(registerUser);
                var res = mvc.perform(get("/v1/user")
                                .header("Authorization", "Bearer " + jwt.getTokenValue()))
                                .andExpect(status().isOk())
                                .andReturn();
                UserResponseDto userResponse = mapper.readValue(res.getResponse().getContentAsString(),
                                UserResponseDto.class);
                assertThat(userResponse).isNotNull();
                assertThat(userResponse.username()).isEqualTo(registerUser.getUsername());
                assertThat(userResponse.userUuid()).isNotEmpty();
        }

        @Test
        void test_get_current_user_status_is_404_if_user_not_found() throws Exception {
                UserDetails registerUser = userService.registerUser("username", "UserUser1234!", "UserUser1234!",
                                "user@test.com",
                                "firstname", "lastname");
                Jwt jwt = jwtService.generateAccessToken(registerUser);
                userRepository.deleteAll();
                mvc.perform(get("/v1/user")
                                .header("Authorization", "Bearer " + jwt.getTokenValue()))
                                .andExpect(status().isNotFound());
        }

        @Test
        void test_update_user_status_is_200_if_successful() throws Exception {
                UserDetails registerUser = userService.registerUser("username", "UserUser1234!", "UserUser1234!",
                                "user@test.com",
                                "firstname", "lastname");
                Jwt jwt = jwtService.generateAccessToken(registerUser);
                var userUpdateDto = new UpdateUserDto("newUsername", "new@test.com", "newFirstname", "newLastname");
                var res = mvc.perform(put("/v1/user")
                                .header("Authorization", "Bearer " + jwt.getTokenValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(userUpdateDto)))
                                .andExpect(status().isOk())
                                .andReturn();
                UserResponseDto userResponse = mapper.readValue(res.getResponse().getContentAsString(),
                                UserResponseDto.class);
                assertThat(userResponse).isNotNull();
                assertThat(userResponse.username()).isEqualTo(userUpdateDto.username());
                assertThat(userResponse.email()).isEqualTo(userUpdateDto.email());
                assertThat(userResponse.firstName()).isEqualTo(userUpdateDto.firstname());
                assertThat(userResponse.lastName()).isEqualTo(userUpdateDto.lastname());
                assertThat(userResponse.userUuid()).isNotEmpty();
        }

        @Test
        void test_update_user_status_is_404_if_user_not_found() throws Exception {
                UserDetails registerUser = userService.registerUser("username", "UserUser1234!", "UserUser1234!",
                                "user@test.com",
                                "firstname", "lastname");
                Jwt jwt = jwtService.generateAccessToken(registerUser);
                var userUpdateDto = new UpdateUserDto("newUsername", "new@test.com", "newFirstname", "newLastname");
                userRepository.deleteAll();
                mvc.perform(put("/v1/user")
                                .header("Authorization", "Bearer " + jwt.getTokenValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(userUpdateDto)))
                                .andExpect(status().isNotFound());
        }
}
