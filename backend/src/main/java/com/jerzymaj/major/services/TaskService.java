package com.jerzymaj.major.services;

import com.jerzymaj.major.Dtos.CreateTaskDto;
import com.jerzymaj.major.models.Task;
import com.jerzymaj.major.models.User;
import com.jerzymaj.major.models.enums.TaskStatus;
import com.jerzymaj.major.repos.TaskRepository;
import com.jerzymaj.major.security.AuthFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final AuthFacade authFacade;
    private final TaskRepository taskRepository;

    public Task createTask(CreateTaskDto createTaskDto) {
        User creator = authFacade.getCurrentUser();

        Task task = Task.builder()
                .title(createTaskDto.title())
                .description(createTaskDto.description())
                .status(TaskStatus.BACKLOG)
                .createdBy(creator)
                .assignee(createTaskDto.assignee() != null ? createTaskDto.assignee() : null)
                .build();

        return taskRepository.save(task);
    }


}