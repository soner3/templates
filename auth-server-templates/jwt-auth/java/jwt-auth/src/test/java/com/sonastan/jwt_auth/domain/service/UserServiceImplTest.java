package com.sonastan.jwt_auth.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.sonastan.jwt_auth.TestcontainersConfiguration;
import com.sonastan.jwt_auth.application.service.UserService;
import com.sonastan.jwt_auth.domain.model.Role;
import com.sonastan.jwt_auth.domain.repository.ProfileRepository;
import com.sonastan.jwt_auth.domain.repository.RoleRepository;
import com.sonastan.jwt_auth.domain.repository.UserRepository;
import com.sonastan.jwt_auth.infrastructure.constants.UserRole;
import com.sonastan.jwt_auth.infrastructure.exception.IllegalModelArgumentException;
import com.sonastan.jwt_auth.infrastructure.exception.NotFoundException;
import com.sonastan.jwt_auth.infrastructure.exception.ServerException;
import com.sonastan.jwt_auth.infrastructure.security.user.UserDetailsImpl;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class UserServiceImplTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    ProfileRepository profileRepository;

    @BeforeEach
    void setUp() {
        profileRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        profileRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void test_user_deleted_if_user_exists() {
        roleRepository.save(new Role(UserRole.ROLE_USER));
        UserDetails user = userService.registerUser("username", "UserUser1234!", "UserUser1234!", "email", "firstname",
                "lastname");
        assertThat(user).isInstanceOf(UserDetailsImpl.class);
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) user;
        userService.deleteUser(userDetailsImpl.getUserUuid());
        assertThat(userRepository.findByUserUuid(userDetailsImpl.getUserUuid()).isPresent()).isFalse();
    }

    @Test
    void test_throw_notfoundexception_if_user_does_not_exist() {
        assertThrows(NotFoundException.class, () -> userService.deleteUser(UUID.randomUUID().toString()));
    }

    @Test
    void test_user_exists_by_username_return_user() {
        roleRepository.save(new Role(UserRole.ROLE_USER));
        String username = "username";
        UserDetails user = userService.registerUser(username, "UserUser1234!", "UserUser1234!", "email", "firstname",
                "lastname");
        UserDetails loadedUser = userService.loadUserByUsername(username);
        assertThat(loadedUser).isInstanceOf(UserDetailsImpl.class);
        assertThat(loadedUser.getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    void test_throw_usernamenotfoundexception_if_username_not_found() {
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("unknown"));
    }

    @Test
    void test_user_exists_by_uuid_return_user() {
        roleRepository.save(new Role(UserRole.ROLE_USER));
        UserDetails user = userService.registerUser("username", "UserUser1234!", "UserUser1234!", "email", "firstname",
                "lastname");
        assertThat(user).isInstanceOf(UserDetailsImpl.class);
        UserDetailsImpl bfLoadUser = (UserDetailsImpl) user;
        assertThat(bfLoadUser).isInstanceOf(UserDetailsImpl.class);
        UserDetails loadedUser = userService.loadUserByUuid(bfLoadUser.getUserUuid());
        assertThat(loadedUser).isInstanceOf(UserDetailsImpl.class);

        UserDetailsImpl afLoadUser = (UserDetailsImpl) loadedUser;
        assertThat(afLoadUser.getUserUuid()).isEqualTo(bfLoadUser.getUserUuid());
    }

    @Test
    void test_throw_notfoundexception_if_user_by_uuid_not_found() {
        assertThrows(NotFoundException.class, () -> userService.loadUserByUuid(UUID.randomUUID().toString()));
    }

    @Test
    void test_user_registered_successfully() {
        roleRepository.save(new Role(UserRole.ROLE_USER));
        UserDetails user = userService.registerUser("username", "UserUser1234!", "UserUser1234!", "email", "firstname",
                "lastname");
        assertThat(user).isInstanceOf(UserDetailsImpl.class);
        assertThat(user.getUsername()).isEqualTo("username");
    }

    @Test
    void test_throw_illegalmodelargumentexception_if_passwords_do_not_match() {
        assertThrows(IllegalModelArgumentException.class,
                () -> userService.registerUser("username", "UserUser1234!", "user", "email", "firstname",
                        "lastname"));
    }

    @Test
    void test_throw_illegalmodelargumentexception_if_password_is_compromised() {
        assertThrows(IllegalModelArgumentException.class,
                () -> userService.registerUser("username", "1234", "1234", "email", "firstname",
                        "lastname"));
    }

    @Test
    void test_throw_illegalmodelargumentexception_if_username_already_exists() {
        roleRepository.save(new Role(UserRole.ROLE_USER));
        UserDetails user = userService.registerUser("username", "UserUser1234!", "UserUser1234!", "email1", "firstname",
                "lastname");
        assertThat(user).isInstanceOf(UserDetailsImpl.class);
        assertThat(user.getUsername()).isEqualTo("username");
        assertThrows(IllegalModelArgumentException.class,
                () -> userService.registerUser("username", "UserUser1234!", "UserUser1234!", "email2", "firstname",
                        "lastname"));
    }

    @Test
    void test_throw_illegalmodelargumentexception_if_email_already_exists() {
        roleRepository.save(new Role(UserRole.ROLE_USER));
        UserDetails user = userService.registerUser("username1", "UserUser1234!", "UserUser1234!", "email", "firstname",
                "lastname");
        assertThat(user).isInstanceOf(UserDetailsImpl.class);
        assertThat(user.getUsername()).isEqualTo("username1");
        assertThrows(IllegalModelArgumentException.class,
                () -> userService.registerUser("username2", "UserUser1234!", "UserUser1234!", "email", "firstname",
                        "lastname"));
    }

    @Test
    void test_throw_serverexception_if_role_not_found() {
        assertThrows(ServerException.class,
                () -> userService.registerUser("username2", "UserUser1234!", "UserUser1234!", "email", "firstname",
                        "lastname"));
    }

    @Test
    void test_updated_user_successfully() {
        roleRepository.save(new Role(UserRole.ROLE_USER));
        UserDetails user = userService.registerUser("username", "UserUser1234!", "UserUser1234!", "email", "firstname",
                "lastname");
        assertThat(user).isInstanceOf(UserDetailsImpl.class);
        UserDetailsImpl userDetails = (UserDetailsImpl) user;
        String newUsername = "newUsername";
        String newEmail = "newEmail";
        UserDetails updatedUser = userService.updateUser(userDetails.getUserUuid(), newUsername, newEmail,
                userDetails.getFirstname(), userDetails.getLastname());
        assertThat(updatedUser).isInstanceOf(UserDetailsImpl.class);
        UserDetailsImpl updatedUserDetails = (UserDetailsImpl) updatedUser;
        assertThat(updatedUserDetails.getUsername()).isEqualTo(newUsername);
        assertThat(updatedUserDetails.getEmail()).isEqualTo(newEmail);
        assertThat(updatedUserDetails.getFirstname()).isEqualTo(userDetails.getFirstname());
        assertThat(updatedUserDetails.getLastname()).isEqualTo(userDetails.getLastname());
    }

    @Test
    void test_throw_illegalmodelargumentexception_if_update_username_already_exists() {
        roleRepository.save(new Role(UserRole.ROLE_USER));
        UserDetails user1 = userService.registerUser("username1", "UserUser1234!", "UserUser1234!", "email1",
                "firstname",
                "lastname");
        UserDetails user2 = userService.registerUser("username2", "UserUser1234!", "UserUser1234!", "email2",
                "firstname",
                "lastname");
        assertThat(user1).isInstanceOf(UserDetailsImpl.class);
        assertThat(user2).isInstanceOf(UserDetailsImpl.class);
        UserDetailsImpl userDetails2 = (UserDetailsImpl) user2;

        String newUsername = "username1";

        assertThrows(IllegalModelArgumentException.class,
                () -> userService.updateUser(userDetails2.getUserUuid(), newUsername, userDetails2.getEmail(),
                        userDetails2.getFirstname(), userDetails2.getLastname()));
    }

    @Test
    void test_throw_illegalmodelargumentexception_if_update_email_already_exists() {
        roleRepository.save(new Role(UserRole.ROLE_USER));
        UserDetails user1 = userService.registerUser("username1", "UserUser1234!", "UserUser1234!", "email1",
                "firstname",
                "lastname");
        UserDetails user2 = userService.registerUser("username2", "UserUser1234!", "UserUser1234!", "email2",
                "firstname",
                "lastname");
        assertThat(user1).isInstanceOf(UserDetailsImpl.class);
        assertThat(user2).isInstanceOf(UserDetailsImpl.class);
        UserDetailsImpl userDetails2 = (UserDetailsImpl) user2;

        String newEmail = "email1";

        assertThrows(IllegalModelArgumentException.class,
                () -> userService.updateUser(userDetails2.getUserUuid(), userDetails2.getUsername(), newEmail,
                        userDetails2.getFirstname(), userDetails2.getLastname()));
    }

    @Test
    void test_throw_notfoundexception_if_update_user_by_uuid_not_found() {
        assertThrows(NotFoundException.class, () -> userService.updateUser(UUID.randomUUID().toString(), "newUsername",
                "newEmail", "newFirstname", "newLastname"));
    }
}
