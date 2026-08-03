package com.jerzymaj.major.Dtos;

import com.jerzymaj.major.models.enums.ChangeType;
import jakarta.validation.constraints.NotNull;

public record CreateActivityLogDto(@NotNull ChangeType changeType, String oldValue, @NotNull String newValue,
                                   @NotNull String changedBy, @NotNull Long taskId) {
}
