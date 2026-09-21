package com.ayeshamart.exception;

/**
 * Thrown by the service layer when user or product input is invalid.
 * The controller catches it and re-displays the form with the message.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}