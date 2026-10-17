package com.jerzymaj.major.exceptions;

public class WeeklySummaryNotFoundException extends RuntimeException {
    public WeeklySummaryNotFoundException(String message) {
        super(message);
    }
}
