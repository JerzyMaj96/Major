package com.jerzymaj.major.Dtos;

import jakarta.validation.constraints.NotBlank;

public record CreateTaskDto(@NotBlank String title, @NotBlank String description, Long assigneeId) {
}
