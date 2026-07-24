package com.jerzymaj.major.exceptions;

import java.time.LocalDateTime;

public record ErrorDetails(LocalDateTime timeStamp, String description,
                           String details) {
}
