package com.tk.learn.model.exceptions;

/**
 * Exception thrown when JWT token validation fails.
 */
public class InvalidJwtTokenException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidJwtTokenException(String message) {
        super(message);
    }

    public InvalidJwtTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}

