package com.sonastan.jwt_auth.infrastructure.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String msg) {
        super(msg);
    }

}
