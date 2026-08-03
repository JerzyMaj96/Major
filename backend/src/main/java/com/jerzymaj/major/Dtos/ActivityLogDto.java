package com.jerzymaj.major.Dtos;

import com.jerzymaj.major.models.enums.ChangeType;

public record ActivityLogDto(Long id, ChangeType changeType, String oldValue, String newValue, String timeStamp,
                             String changedBy) {
}
