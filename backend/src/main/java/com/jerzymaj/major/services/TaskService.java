package com.jerzymaj.major.services;

import com.jerzymaj.major.Dtos.CreateTaskDto;
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

@Service
@RequiredArgsConstructor
public class TaskService {

    private final AuthFacade authFacade;
    private final UserService userService;
    private final TaskRepository taskRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public Task createTask(CreateTaskDto createTaskDto) {
        User creator = authFacade.getCurrentUser();

        User assignee = createTaskDto.assigneeId() != null ? userService.findUserById(createTaskDto.assigneeId()) : null;

        Task task = Task.builder()
                .title(createTaskDto.title())
                .description(createTaskDto.description())
                .status(TaskStatus.BACKLOG)
                .createdBy(creator)
                .assignee(assignee)
                .build();

        return taskRepository.save(task);
    }

    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }

    public Task addAssignee(Long taskId, Long assigneeId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        User assignee = userService.findUserById(assigneeId);

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
}