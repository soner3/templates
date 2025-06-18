package com.sonastan.jwt_auth.infrastructure.exception;

public class IllegalModelArgumentException extends IllegalArgumentException {

    public IllegalModelArgumentException(String message) {
        super(message);
    }
}
