package com.jerzymaj.major.exceptions;

public class AiGenerationException extends RuntimeException {
    public AiGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
