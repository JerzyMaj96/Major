package com.jerzymaj.major.Dtos;

import com.jerzymaj.major.models.enums.ChangeType;

import java.time.LocalDateTime;

public record GlobalActivityLogDto(
        Long id,
        Long taskId,
        String taskTitle,
        ChangeType changeType,
        String oldValue,
        String newValue,
        LocalDateTime createdAt,
        String changedBy
) {
}
