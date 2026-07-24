package com.jerzymaj.major.Dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserDto(@NotBlank String name, @Email @NotBlank String email, @NotBlank
@Size(min = 8) String password) {
}
