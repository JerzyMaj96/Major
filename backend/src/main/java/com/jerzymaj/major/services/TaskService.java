package com.jerzymaj.major.services;

import com.jerzymaj.major.Dtos.CreateTaskDto;
import com.jerzymaj.major.Dtos.UpdateTaskDto;
import com.jerzymaj.major.exceptions.AssigneeMismatchException;
import com.jerzymaj.major.exceptions.TaskNotFoundException;
import com.jerzymaj.major.mappers.TaskMapper;
import com.jerzymaj.major.models.Label;
import com.jerzymaj.major.models.Task;
import com.jerzymaj.major.models.User;
import com.jerzymaj.major.models.enums.ChangeType;
import com.jerzymaj.major.models.enums.TaskStatus;
import com.jerzymaj.major.repos.TaskRepository;
import com.jerzymaj.major.security.AuthFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final AuthFacade authFacade;
    private final UserService userService;
    private final LabelService labelService;
    private final GptService gptService;
    private final ActivityLogService activityLogService;
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

        Task savedTask = taskRepository.save(task);

        activityLogService.createActivityLog(savedTask, ChangeType.TASK_CHANGE, null,
                "Task created", creator.getName());

        return savedTask;
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

        String oldTitle = task.getTitle();
        String oldDescription = task.getDescription();

        if (updateTaskDto.title() != null) {
            task.setTitle(updateTaskDto.title());
        }

        if (updateTaskDto.generateDescription()) {
            task.setDescription(gptService.generateTaskDescription(task.getTitle()));
        } else if (updateTaskDto.description() != null) {
            task.setDescription(updateTaskDto.description());
        }

        Task savedTask = taskRepository.save(task);

        if (updateTaskDto.title() != null) {
            activityLogService.createActivityLog(savedTask, ChangeType.TASK_CHANGE,
                    oldTitle, savedTask.getTitle(), authFacade.getCurrentUser().getName());
        }
        if (updateTaskDto.description() != null || updateTaskDto.generateDescription()) {
            activityLogService.createActivityLog(savedTask, ChangeType.TASK_CHANGE,
                    oldDescription, savedTask.getDescription(), authFacade.getCurrentUser().getName());
        }


        return savedTask;
    }

    public void deleteTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        taskRepository.delete(task);
    }

    public Task addAssignee(Long taskId, Long assigneeId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        User assignee = userService.getUserById(assigneeId);

        task.setAssignee(assignee);

        Task savedTask = taskRepository.save(task);

        activityLogService.createActivityLog(savedTask, ChangeType.ASSIGNEE_CHANGE, null,
                "Assignee added: " + assignee.getName(), authFacade.getCurrentUser().getName());

        return savedTask;
    }

    public void deleteAssignee(Long taskId, Long assigneeId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        if (task.getAssignee() != null && task.getAssignee().getId().equals(assigneeId)) {
            task.setAssignee(null);
            Task savedTask = taskRepository.save(task);

            activityLogService.createActivityLog(savedTask, ChangeType.ASSIGNEE_CHANGE, null,
                    "Assignee removed", authFacade.getCurrentUser().getName());
        } else {
            throw new AssigneeMismatchException("Assignee with id: " + assigneeId + " is not assigned to the task with id: " + taskId);
        }
    }

    public Task updateTaskStatus(Long taskId, TaskStatus taskStatus, String changedBy) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        TaskStatus status = task.getStatus();
        task.setStatus(taskStatus);
        Task savedTask = taskRepository.save(task);

        simpMessagingTemplate.convertAndSend("/topic/task-updates", TaskMapper.toDto(savedTask));

        activityLogService.createActivityLog(savedTask, ChangeType.STATUS_CHANGE, status.toString(),
                taskStatus.name(), changedBy);

        return savedTask;
    }

    public Task updateTaskStatus(Long taskId, TaskStatus taskStatus) {
        return updateTaskStatus(taskId, taskStatus, authFacade.getCurrentUser().getName());
    }

    public Task addLabelToTask(Long taskId, Long labelId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        task.getLabels().add(labelService.getLabelById(labelId));
        Task savedTask = taskRepository.save(task);

        activityLogService.createActivityLog(savedTask, ChangeType.LABEL_CHANGE, null,
                "Label added: " + labelService.getLabelById(labelId).getName(), authFacade.getCurrentUser().getName());

        return savedTask;
    }

    public Task deleteLabelFromTask(Long taskId, Long labelId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        Label label = labelService.getLabelById(labelId);
        task.getLabels().removeIf(l -> l.getId().equals(labelId));
        Task savedTask = taskRepository.save(task);

        activityLogService.createActivityLog(savedTask, ChangeType.LABEL_CHANGE, null,
                "Label removed: " + label.getName(), authFacade.getCurrentUser().getName());

        return savedTask;
    }
}