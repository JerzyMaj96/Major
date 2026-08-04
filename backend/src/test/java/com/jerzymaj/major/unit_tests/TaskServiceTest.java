package com.jerzymaj.major.unit_tests;

import com.jerzymaj.major.Dtos.CreateTaskDto;
import com.jerzymaj.major.Dtos.TaskDto;
import com.jerzymaj.major.Dtos.UpdateTaskDto;
import com.jerzymaj.major.models.Label;
import com.jerzymaj.major.models.Task;
import com.jerzymaj.major.models.User;
import com.jerzymaj.major.models.enums.ChangeType;
import com.jerzymaj.major.models.enums.TaskStatus;
import com.jerzymaj.major.models.enums.UserRole;
import com.jerzymaj.major.repos.TaskRepository;
import com.jerzymaj.major.security.AuthFacade;
import com.jerzymaj.major.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @Mock
    private LabelService labelService;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private GptService gptService;

    @Mock
    private AuthFacade authFacade;

    @InjectMocks
    private TaskService taskService;

    private User creator;
    private User assignee;
    private Task expectedTask;

    @BeforeEach
    public void setUp() {
        creator = new User(1L, "jerzy", "jerzy@mail.com", "password1", UserRole.USER, LocalDateTime.now());
        assignee = new User(2L, "assignee", "assignee@mail.com", "password2", UserRole.USER, LocalDateTime.now());

        expectedTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Generated description")
                .status(TaskStatus.BACKLOG)
                .createdBy(creator)
                .assignee(assignee)
                .build();

        lenient().when(authFacade.getCurrentUser()).thenReturn(creator);
        lenient().when(taskRepository.findById(1L)).thenReturn(Optional.of(expectedTask));
        lenient().when(userService.getUserById(2L)).thenReturn(assignee);
        lenient().when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        lenient().when(activityLogService.createActivityLog(any(Task.class), any(ChangeType.class), any(String.class),
                any(String.class), any(String.class))).thenReturn(null);
        lenient().when(labelService.getLabelById(1L)).thenReturn(new Label(1L, "Label 1", "#FFFFFF"));
    }

    @Test
    public void shouldCreateTaskWithGenDescription_IfSuccess() {
        when(gptService.generateTaskDescription("Test Task")).thenReturn("Generated description");

        Task actualResult = taskService.createTask(new CreateTaskDto("Test Task", null, 2L, true));

        assertThat(actualResult.getTitle()).isEqualTo("Test Task");
        assertThat(actualResult.getDescription()).isEqualTo("Generated description");
        assertThat(actualResult.getAssignee().getId()).isEqualTo(2L);
    }

    @Test
    public void shouldUpdateTask_IfSuccess() {
        UpdateTaskDto updateTaskDto = new UpdateTaskDto("Updated Task", "Updated description", false);

        Task actualResult = taskService.updateTask(1L, updateTaskDto);

        assertThat(actualResult.getTitle()).isEqualTo("Updated Task");
        assertThat(actualResult.getDescription()).isEqualTo("Updated description");
    }

    @Test
    public void shouldDeleteTask_IfSuccess() {
        taskService.deleteTaskById(1L);

        verify(taskRepository).delete(expectedTask);
    }

    @Test
    public void shouldAddAssignee_IfSuccess() {

        Task actualResult = taskService.addAssignee(1L, 2L);

        assertThat(actualResult.getAssignee().getId()).isEqualTo(2L);
    }

    @Test
    public void shouldDeleteAssignee_IfSuccess() {
        taskService.deleteAssignee(1L, 2L);

        assertThat(taskRepository.findById(1L).orElseThrow().getAssignee()).isNull();
    }

    @Test
    public void shouldUpdateTaskStatus_IfSuccess() {

        Task actualResult = taskService.updateTaskStatus(1L, TaskStatus.IN_PROGRESS);

        assertThat(actualResult.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);

        verify(simpMessagingTemplate).convertAndSend(eq("/topic/task-updates"), any(TaskDto.class));
    }

    @Test
    public void shouldAddLabel_IfSuccess() {

        Task actualResult = taskService.addLabelToTask(1L, 1L);

        assertThat(actualResult.getLabels()).hasSize(1);
    }

    @Test
    public void shouldDeleteLabel_IfSuccess() {
        Label label = new Label(1L, "Label 1", "#FFFFFF");
        expectedTask.getLabels().add(label);

        taskService.deleteLabelFromTask(1L, 1L);

        assertThat(taskRepository.findById(1L).orElseThrow().getLabels()).isEmpty();
    }
}
