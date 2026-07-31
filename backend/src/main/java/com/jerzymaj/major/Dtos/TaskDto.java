package com.jerzymaj.major.Dtos;

import com.jerzymaj.major.models.enums.TaskStatus;

import java.time.LocalDateTime;
import java.util.Set;

public record TaskDto(Long id,
                      String title,
                      String description,
                      TaskStatus status,
                      UserSummaryDto assignee,
                      UserSummaryDto createdBy,
                      Set<LabelDto> labels,
                      LocalDateTime createdAt,
                      LocalDateTime updatedAt) {
}
