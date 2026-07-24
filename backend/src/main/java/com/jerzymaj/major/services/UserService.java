package com.jerzymaj.major.services;

import com.jerzymaj.major.Dtos.RegisterUserDto;
import com.jerzymaj.major.exceptions.ExistingUserException;
import com.jerzymaj.major.models.User;
import com.jerzymaj.major.repos.UserRepository;
import com.jerzymaj.major.security.AuthFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthFacade authFacade;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(RegisterUserDto registerUserDto) {

        if (userRepository.existsByName(registerUserDto.name())) {
            throw new ExistingUserException("Name '" + registerUserDto.name() + "' is already taken");
        }

        if (userRepository.existsByEmail(registerUserDto.email())) {
            throw new ExistingUserException("Email '" + registerUserDto.email() + "' is already taken");
        }

        User user = User.builder()
                .name(registerUserDto.name())
                .email(registerUserDto.email())
                .password(passwordEncoder.encode(registerUserDto.password()))
                .build();

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ExistingUserException("Name '" + registerUserDto.name()
                    + "' or email '" + registerUserDto.email() + "' is already taken");
        }
    }

    public void deleteCurrUser() {
        User user = authFacade.getCurrentUser();
        userRepository.delete(user);
    }

}
