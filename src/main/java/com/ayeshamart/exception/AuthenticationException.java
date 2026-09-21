package com.ayeshamart.exception;

/**
 * Thrown by the service layer when login credentials do not match a user.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }
}