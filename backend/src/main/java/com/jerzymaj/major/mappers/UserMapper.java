package com.jerzymaj.major.mappers;

import com.jerzymaj.major.Dtos.UserDto;
import com.jerzymaj.major.models.User;

public class UserMapper {

    private UserMapper() {
    }

    public static UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreationDate()
        );
    }
}
