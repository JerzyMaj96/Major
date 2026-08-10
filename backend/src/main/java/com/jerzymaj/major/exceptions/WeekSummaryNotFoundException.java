package com.jerzymaj.major.exceptions;

public class WeekSummaryNotFoundException extends RuntimeException {
    public WeekSummaryNotFoundException(String message) {
        super(message);
    }
}
