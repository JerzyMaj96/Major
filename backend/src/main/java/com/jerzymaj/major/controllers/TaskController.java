package com.jerzymaj.major.controllers;

import com.jerzymaj.major.Dtos.CreateTaskDto;
import com.jerzymaj.major.Dtos.TaskDto;
import com.jerzymaj.major.Dtos.UpdateTaskDto;
import com.jerzymaj.major.mappers.TaskMapper;
import com.jerzymaj.major.models.enums.TaskStatus;
import com.jerzymaj.major.services.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("major/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody CreateTaskDto createTaskDto) {
        TaskDto taskDto = TaskMapper.toDto(taskService.createTask(createTaskDto));

        return ResponseEntity.created(URI.create("/major/api/tasks/" + taskDto.id())).body(taskDto);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDto> retrieveTaskById(@PathVariable("taskId") Long taskId) {
        TaskDto taskDto = TaskMapper.toDto(taskService.getTaskById(taskId));

        return ResponseEntity.ok(taskDto);
    }

    @GetMapping
    public ResponseEntity<List<TaskDto>> retrieveAllTasks() {
        List<TaskDto> taskDtoList = taskService.getAllTasks().stream()
                .map(TaskMapper::toDto)
                .toList();
        return ResponseEntity.ok(taskDtoList);
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable("taskId") Long taskId, @RequestBody UpdateTaskDto updateTaskDto) {
        TaskDto taskDto = TaskMapper.toDto(taskService.updateTask(taskId, updateTaskDto));

        return ResponseEntity.ok(taskDto);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable("taskId") Long taskId) {
        taskService.deleteTaskById(taskId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{taskId}/assignees/{assigneeId}")
    public ResponseEntity<TaskDto> assignTask(@PathVariable("taskId") Long taskId, @PathVariable("assigneeId") Long assigneeId) {
        TaskDto taskDto = TaskMapper.toDto(taskService.addAssignee(taskId, assigneeId));

        return ResponseEntity.ok(taskDto);
    }

    @DeleteMapping("/{taskId}/assignees/{assigneeId}")
    public ResponseEntity<Void> removeAssignee(@PathVariable("taskId") Long taskId, @PathVariable("assigneeId") Long assigneeId) {
        taskService.deleteAssignee(taskId, assigneeId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskDto> updateTaskStatus(@PathVariable("taskId") Long taskId, @RequestParam("taskStatus") TaskStatus taskStatus) {
        TaskDto taskDto = TaskMapper.toDto(taskService.updateTaskStatus(taskId, taskStatus));

        return ResponseEntity.ok(taskDto);
    }

    @PatchMapping("/{taskId}/labels/{labelId}")
    public ResponseEntity<TaskDto> addLabelToTask(@PathVariable("taskId") Long taskId, @PathVariable("labelId") Long labelId) {
        TaskDto taskDto = TaskMapper.toDto(taskService.addLabelToTask(taskId, labelId));

        return ResponseEntity.ok(taskDto);
    }

    @DeleteMapping("/{taskId}/labels/{labelId}")
    public ResponseEntity<Void> removeLabelFromTask(@PathVariable("taskId") Long taskId, @PathVariable("labelId") Long labelId) {
        taskService.deleteLabelFromTask(taskId, labelId);

        return ResponseEntity.noContent().build();
    }
}
