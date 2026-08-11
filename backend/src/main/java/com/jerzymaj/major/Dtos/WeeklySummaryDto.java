package com.jerzymaj.major.Dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record WeeklySummaryDto(Long id, String content, Long tasksCompleted, Long tasksCreated,
                               LocalDate periodStart, LocalDate periodEnd,
                               LocalDateTime generatedAt) {
}
