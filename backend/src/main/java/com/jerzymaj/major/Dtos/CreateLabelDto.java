package com.jerzymaj.major.Dtos;

import jakarta.validation.constraints.NotBlank;

public record CreateLabelDto(@NotBlank String name,@NotBlank String color) {
}
