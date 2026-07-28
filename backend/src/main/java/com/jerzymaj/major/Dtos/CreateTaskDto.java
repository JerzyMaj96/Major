package com.jerzymaj.major.Dtos;

import com.jerzymaj.major.models.User;
import jakarta.validation.constraints.NotBlank;

public record CreateTaskDto(@NotBlank String title, @NotBlank String description, User assignee) {
}
