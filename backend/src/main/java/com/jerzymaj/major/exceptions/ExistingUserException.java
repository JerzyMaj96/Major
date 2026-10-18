package com.jerzymaj.major.exceptions;

public class ExistingUserException extends RuntimeException {
    public ExistingUserException(String message) {
        super(message);
    }

    public ExistingUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
