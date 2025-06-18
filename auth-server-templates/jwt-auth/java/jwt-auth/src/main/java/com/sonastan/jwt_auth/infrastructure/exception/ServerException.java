package com.sonastan.jwt_auth.infrastructure.exception;

public class ServerException extends RuntimeException {

    public ServerException(String message) {
        super(message);
    }

}
