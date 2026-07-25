package com.jerzymaj.major.unit_tests;

import com.jerzymaj.major.Dtos.RegisterUserDto;
import com.jerzymaj.major.exceptions.ExistingUserException;
import com.jerzymaj.major.models.User;
import com.jerzymaj.major.repos.UserRepository;
import com.jerzymaj.major.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    public void shouldRegisterUser_ifSuccess() {
        RegisterUserDto registerUserDTO = new RegisterUserDto("jerzy", "jerzy@mail.com", "secret123");

        when(userRepository.existsByName(registerUserDTO.name())).thenReturn(false);
        when(userRepository.existsByEmail(registerUserDTO.email())).thenReturn(false);
        when(passwordEncoder.encode(registerUserDTO.password())).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(invocationOnMock -> {
            User user = invocationOnMock.getArgument(0);
            user.setId(1L);
            return user;
        });

        User actualResult = userService.registerUser(registerUserDTO);

        assertThat(actualResult.getId()).isEqualTo(1L);
        assertThat(actualResult.getEmail()).isEqualTo(registerUserDTO.email());
        verify(passwordEncoder).encode(registerUserDTO.password());
    }

    @Test
    public void shouldThrowExistingUserException_IfUserExists() {
        when(userRepository.existsByName("jerzy")).thenReturn(true);

        RegisterUserDto registerUserDTO = new RegisterUserDto("jerzy", "x@y.com", "password");

        assertThatThrownBy(() -> userService.registerUser(registerUserDTO))
                .isInstanceOf(ExistingUserException.class);
    }
}
