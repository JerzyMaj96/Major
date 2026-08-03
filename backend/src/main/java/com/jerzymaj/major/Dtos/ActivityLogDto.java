package com.jerzymaj.major.Dtos;

import com.jerzymaj.major.models.enums.ChangeType;

import java.time.LocalDateTime;

public record ActivityLogDto(Long id, ChangeType changeType, String oldValue, String newValue, LocalDateTime timeStamp,
                             String changedBy) {
}
