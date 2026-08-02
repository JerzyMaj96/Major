package com.jerzymaj.major.services;

import com.jerzymaj.major.Dtos.CreateTaskDto;
import com.jerzymaj.major.Dtos.UpdateTaskDto;
import com.jerzymaj.major.exceptions.AssigneeMismatchException;
import com.jerzymaj.major.exceptions.TaskNotFoundException;
import com.jerzymaj.major.mappers.TaskMapper;
import com.jerzymaj.major.models.Task;
import com.jerzymaj.major.models.User;
import com.jerzymaj.major.models.enums.TaskStatus;
import com.jerzymaj.major.repos.TaskRepository;
import com.jerzymaj.major.security.AuthFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final AuthFacade authFacade;
    private final UserService userService;
    private final LabelService labelService;
    private final GptService gptService;
    private final TaskRepository taskRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public Task createTask(CreateTaskDto createTaskDto) {
        User creator = authFacade.getCurrentUser();

        User assignee = createTaskDto.assigneeId() != null ? userService.getUserById(createTaskDto.assigneeId()) : null;

        String description;
        if (createTaskDto.generateDescription()) {
            description = gptService.generateTaskDescription(createTaskDto.title());
        } else if (createTaskDto.description() != null && !createTaskDto.description().isBlank()) {
            description = createTaskDto.description();
        } else {
            throw new IllegalArgumentException("Description must be provided if generateDescription is false");
        }

        Task task = Task.builder()
                .title(createTaskDto.title())
                .description(description)
                .status(TaskStatus.BACKLOG)
                .createdBy(creator)
                .assignee(assignee)
                .build();

        return taskRepository.save(task);
    }

    public Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task updateTask(Long taskId, UpdateTaskDto updateTaskDto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        if (updateTaskDto.title() != null) {
            task.setTitle(updateTaskDto.title());
        }

        if (updateTaskDto.generateDescription()) {
            task.setDescription(gptService.generateTaskDescription(task.getTitle()));
        } else if (updateTaskDto.description() != null) {
            task.setDescription(updateTaskDto.description());
        }


        return taskRepository.save(task);
    }

    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        taskRepository.delete(task);
    }

    public Task addAssignee(Long taskId, Long assigneeId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        User assignee = userService.getUserById(assigneeId);

        task.setAssignee(assignee);
        return taskRepository.save(task);
    }

    public void deleteAssignee(Long taskId, Long assigneeId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        if (task.getAssignee() != null && task.getAssignee().getId().equals(assigneeId)) {
            task.setAssignee(null);
            taskRepository.save(task);
        } else {
            throw new AssigneeMismatchException("Assignee with id: " + assigneeId + " is not assigned to the task with id: " + taskId);
        }
    }

    public Task updateTaskStatus(Long taskId, TaskStatus taskStatus) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        task.setStatus(taskStatus);
        Task savedTask = taskRepository.save(task);

        simpMessagingTemplate.convertAndSend("/topic/task-updates", TaskMapper.toDto(savedTask));

        return savedTask;
    }

    public Task addLabelToTask(Long taskId, Long labelId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        task.getLabels().add(labelService.getLabelById(labelId));
        return taskRepository.save(task);
    }

    public Task deleteLabelFromTask(Long taskId, Long labelId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        task.getLabels().removeIf(label -> label.getId().equals(labelId));
        return taskRepository.save(task);
    }
}