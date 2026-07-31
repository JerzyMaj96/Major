package com.jerzymaj.major.exceptions;

public class ExistingLabelNameException extends RuntimeException {
    public ExistingLabelNameException(String message) {
        super(message);
    }
}
