package com.jerzymaj.major.Dtos;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank(message = "Name is required") String name,
                           @NotBlank(message = "Password is required")
                           String password) {
}
