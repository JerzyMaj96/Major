package com.jerzymaj.major.integration_tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jerzymaj.major.Dtos.CreateTaskDto;
import com.jerzymaj.major.Dtos.UpdateTaskDto;
import com.jerzymaj.major.configuration.WithMockCustomUser;
import com.jerzymaj.major.models.Label;
import com.jerzymaj.major.models.Task;
import com.jerzymaj.major.models.User;
import com.jerzymaj.major.models.enums.TaskStatus;
import com.jerzymaj.major.models.enums.UserRole;
import com.jerzymaj.major.repos.LabelRepository;
import com.jerzymaj.major.repos.TaskRepository;
import com.jerzymaj.major.repos.UserRepository;
import com.jerzymaj.major.services.GptService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @Autowired
    private LabelRepository labelRepository;

    @MockitoBean
    private GptService gptService;

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private CreateTaskDto createTaskDto;
    private Task testTask;
    private User assignee;
    private Label testLabel;

    @BeforeEach
    public void setup() {
        User existingUser = userRepository.findByName("tester")
                .orElseThrow(() -> new IllegalStateException("Expected test user not found"));

        assignee = User.builder()
                .name("assignee")
                .email("assignee@example.com")
                .password("password2")
                .role(UserRole.USER)
                .build();

        userRepository.save(assignee);

        createTaskDto = new CreateTaskDto(
                "Test Task",
                "This is a test task.",
                existingUser.getId(),
                true
        );

        when(gptService.generateTaskDescription(anyString())).thenReturn("Generated task description.");

        testTask = Task.builder()
                .title("Test Task")
                .description("This is a test task.")
                .status(TaskStatus.BACKLOG)
                .createdBy(existingUser)
                .build();

        taskRepository.save(testTask);

        testLabel = Label.builder()
                .name("Test Label")
                .color("#FF5733")
                .build();

        labelRepository.save(testLabel);
    }

    @Test
    @WithMockCustomUser
    public void createTask() throws Exception {

        mockMvc.perform(post("/major/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(createTaskDto.title()))
                .andExpect(jsonPath("$.description").value("Generated task description."));
    }

    @Test
    @WithMockCustomUser
    public void retrieveTaskById() throws Exception {

        mockMvc.perform(get("/major/api/tasks/{id}", testTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTask.getId()));
    }

    @Test
    @WithMockCustomUser
    public void retrieveAllTasks() throws Exception {

        mockMvc.perform(get("/major/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockCustomUser
    public void updateTask() throws Exception {

        mockMvc.perform(patch("/major/api/tasks/{id}", testTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateTaskDto("Updated Task Title",
                                "Nongenerated task description", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task Title"))
                .andExpect(jsonPath("$.description").value("Nongenerated task description"));
    }

    @Test
    @WithMockCustomUser
    public void deleteTask() throws Exception {

        mockMvc.perform(delete("/major/api/tasks/{id}", testTask.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/major/api/tasks/{id}", testTask.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    public void assignTask() throws Exception {

        mockMvc.perform(patch("/major/api/tasks/{taskId}/assignees/{assigneeId}", testTask.getId(), assignee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignee.id").value(assignee.getId()));
    }

    @Test
    @WithMockCustomUser
    public void removeAssignee() throws Exception {
        testTask.setAssignee(assignee);
        taskRepository.save(testTask);

        mockMvc.perform(delete("/major/api/tasks/{taskId}/assignees/{assigneeId}", testTask.getId(), assignee.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser
    public void updateTaskStatus() throws Exception {

        mockMvc.perform(patch("/major/api/tasks/{taskId}/status", testTask.getId())
                        .param("taskStatus", TaskStatus.IN_PROGRESS.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(TaskStatus.IN_PROGRESS.name()));
    }

    @Test
    @WithMockCustomUser
    public void addLabelToTask() throws Exception {

        mockMvc.perform(patch("/major/api/tasks/{taskId}/labels/{labelId}", testTask.getId(), testLabel.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels", hasSize(1)))
                .andExpect(jsonPath("$.labels[0].id").value(testLabel.getId()));
    }

    @Test
    @WithMockCustomUser
    public void removeLabelFromTask() throws Exception {
        mockMvc.perform(patch("/major/api/tasks/{taskId}/labels/{labelId}", testTask.getId(), testLabel.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/major/api/tasks/{taskId}/labels/{labelId}", testTask.getId(), testLabel.getId()))
                .andExpect(status().isNoContent());
    }
}
