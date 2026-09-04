package com.jerzymaj.major.integration_tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jerzymaj.major.Dtos.LoginRequest;
import com.jerzymaj.major.Dtos.RegisterUserDto;
import com.jerzymaj.major.services.GptService;
import com.jerzymaj.major.services.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerIntegration {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserService userService;

    @MockitoBean
    private GptService gptService;

    @BeforeEach
    public void setup() {
        userService.registerUser(new RegisterUserDto("tester", "tester@example.com", "password"));
    }

    @Test
    public void loginWithValidCredentials_returnsToken() throws Exception {

        LoginRequest loginRequest = new LoginRequest("tester", "password");

        mockMvc.perform(post("/major/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(not(emptyString())));
    }

    @Test
    public void loginWithInvalidPassword_returnsUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest("tester", "badpassword");

        mockMvc.perform(post("/major/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithNonExistentUser_returnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent", "password");

        mockMvc.perform(post("/major/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithBlankFields_returnsBadRequest() throws Exception {
        LoginRequest request = new LoginRequest("", "");

        mockMvc.perform(post("/major/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

}
