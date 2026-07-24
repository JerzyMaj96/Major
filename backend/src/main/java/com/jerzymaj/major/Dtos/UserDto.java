package com.jerzymaj.major.Dtos;

import java.time.LocalDateTime;

public record UserDto(Long id,
                      String name,
                      String email,
                      LocalDateTime creationDate) {
}
