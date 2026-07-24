package com.jerzymaj.major.exceptions;

public class ExistingUserException extends RuntimeException {
    public ExistingUserException(String message) {
        super(message);
    }
}
