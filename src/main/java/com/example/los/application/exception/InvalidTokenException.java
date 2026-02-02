package com.example.los.application.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends JwtAuthenticationException {

    public InvalidTokenException(String msg) {
        super(msg, HttpStatus.UNAUTHORIZED);
    }

    public InvalidTokenException(String msg, Throwable cause) {
        super(msg, cause, HttpStatus.UNAUTHORIZED);
    }
}