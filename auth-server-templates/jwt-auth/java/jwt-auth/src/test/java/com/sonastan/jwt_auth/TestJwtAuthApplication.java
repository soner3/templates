package com.sonastan.jwt_auth;

import org.springframework.boot.SpringApplication;

public class TestJwtAuthApplication {

    public static void main(String[] args) {
        SpringApplication.from(JwtAuthApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
