package com.jerzymaj.major.Dtos;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank(message = "Name or email is required") String identifier,
                           @NotBlank(message = "Password is required")
                           String password) {
}
