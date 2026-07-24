package com.jerzymaj.major.controllers;

import com.jerzymaj.major.Dtos.RegisterUserDto;
import com.jerzymaj.major.Dtos.UserDto;
import com.jerzymaj.major.mappers.UserMapper;
import com.jerzymaj.major.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("major/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody RegisterUserDto registerUserDto) {
        UserDto userDto = UserMapper.toDto(userService.registerUser(registerUserDto));

        return ResponseEntity.created(URI.create("/major/api/users/" + userDto.id())).body(userDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findUserById(@PathVariable Long id) {
        return ResponseEntity.ok(UserMapper.toDto(userService.findUserById(id)));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrUser() {
        userService.deleteCurrUser();
        return ResponseEntity.noContent().build();
    }
}