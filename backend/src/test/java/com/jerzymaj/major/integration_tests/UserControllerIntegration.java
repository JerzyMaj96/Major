package com.jerzymaj.major.integration_tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jerzymaj.major.Dtos.RegisterUserDto;
import com.jerzymaj.major.configuration.WithMockCustomUser;
import com.jerzymaj.major.models.User;
import com.jerzymaj.major.repos.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserControllerIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        if (userRepository.findByName("tester").isEmpty()) {
            User tester = new User();
            tester.setName("tester");
            tester.setEmail("tester@mail.com");
            tester.setPassword("secret123");
            userRepository.save(tester);
        }
    }

    @Test
    @WithMockCustomUser
    public void shouldRegisterUser() throws Exception {
        RegisterUserDto registerUserDTO = new RegisterUserDto("jerzy", "jerzy@mail.com", "secret123");

        mockMvc.perform(post("/major/api/users/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("jerzy@mail.com"));
    }

    @Test
    @WithMockCustomUser
    public void shouldDeleteCurrUser() throws Exception {

        User tester = userRepository.findByName("tester").orElseThrow();

        mockMvc.perform(delete("/major/api/users/delete-me"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/major/api/users/{userId}", tester.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    public void shouldRetrieveUserById() throws Exception {
        User tester = userRepository.findByName("tester").orElseThrow();

        mockMvc.perform(get("/major/api/users/{userId}", tester.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("tester"))
                .andExpect(jsonPath("$.email").value("tester@mail.com"));
    }
}
