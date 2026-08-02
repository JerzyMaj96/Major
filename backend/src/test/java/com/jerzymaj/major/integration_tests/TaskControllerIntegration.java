package com.jerzymaj.major.integration_tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jerzymaj.major.Dtos.CreateTaskDto;
import com.jerzymaj.major.Dtos.TaskDto;
import com.jerzymaj.major.configuration.WithMockCustomUser;
import com.jerzymaj.major.models.Task;
import com.jerzymaj.major.models.User;
import com.jerzymaj.major.models.enums.TaskStatus;
import com.jerzymaj.major.models.enums.UserRole;
import com.jerzymaj.major.repos.TaskRepository;
import com.jerzymaj.major.repos.UserRepository;
import com.jerzymaj.major.services.GptService;
import com.jerzymaj.major.services.LabelService;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TaskControllerIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @MockitoBean
    private LabelService labelService;

    @MockitoBean
    private GptService gptService;

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private CreateTaskDto createTaskDto;
    private Task testTask;

    @BeforeEach
    public void setup() {
        createTaskDto = new CreateTaskDto(
                "Test Task",
                "This is a test task.",
                1L,
                true
        );

        when(gptService.generateTaskDescription(anyString())).thenReturn("Generated task description.");

        User existingUser = userRepository.findByName("tester")
                .orElseThrow(() -> new IllegalStateException("Expected test user not found"));

        testTask = Task.builder()
                .title("Test Task")
                .description("This is a test task.")
                .status(TaskStatus.BACKLOG)
                .createdBy(existingUser)
                .build();
        taskRepository.save(testTask);
    }

    @Test
    @WithMockCustomUser
    public void createTask() throws Exception {

        mockMvc.perform(post("/major/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskDto)))
                .andExpect(status().isCreated())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    TaskDto responseTask = objectMapper.readValue(responseBody, TaskDto.class);
                    assert responseTask.title().equals(createTaskDto.title());
                    assert responseTask.description().equals("Generated task description.");
                });
    }

    @Test
    @WithMockCustomUser
    public void retrieveTaskById() throws Exception {

        mockMvc.perform(get("/major/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    TaskDto responseTask = objectMapper.readValue(responseBody, TaskDto.class);
                    assert responseTask.id() == 1L;
                });
    }
}
