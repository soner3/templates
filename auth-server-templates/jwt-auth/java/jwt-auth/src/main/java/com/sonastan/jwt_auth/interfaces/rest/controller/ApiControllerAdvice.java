package com.sonastan.jwt_auth.interfaces.rest.controller;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sonastan.jwt_auth.infrastructure.exception.IllegalModelArgumentException;

@RestControllerAdvice
public class ApiControllerAdvice {

    private ResponseEntity<ProblemDetail> createProblemDetail(String title, HttpStatus status, Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        String type = ex.getClass().getName().replace('.', '/');
        problemDetail.setTitle(title);
        problemDetail.setDetail(ex.getMessage());
        problemDetail.setType(URI.create(type));
        problemDetail.setStatus(status.value());
        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = createProblemDetail("Validation Error", HttpStatus.BAD_REQUEST, ex).getBody();
        Map<String, Object> validationErrors = ex
                .getBindingResult()
                .getAllErrors()
                .stream()
                .collect(Collectors.toMap(
                        err -> ((FieldError) err).getField(),
                        err -> err.getDefaultMessage()));
        problemDetail.setDetail("Validation failed for one or more fields");
        problemDetail.setProperties(validationErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleException(Exception ex) {
        return createProblemDetail("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleJwtException(JwtException ex) {
        return createProblemDetail("JWT Error", HttpStatus.UNAUTHORIZED, ex);
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleBadCredentialsException(BadCredentialsException ex) {
        return createProblemDetail("Bad Credentials", HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleIllegalModelArgumentException(IllegalModelArgumentException ex) {
        return createProblemDetail("Invalid Request", HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return createProblemDetail("User Not Found", HttpStatus.NOT_FOUND, ex);
    }

}
