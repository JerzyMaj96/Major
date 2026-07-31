package com.jerzymaj.major.mappers;

import com.jerzymaj.major.Dtos.TaskDto;
import com.jerzymaj.major.models.Task;

import java.util.stream.Collectors;

public final class TaskMapper {

    private TaskMapper() {}

    public static TaskDto toDto(Task task) {
        return new TaskDto(task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getAssignee() != null ? UserMapper.toSummaryDto(task.getAssignee()) : null,
                UserMapper.toSummaryDto(task.getCreatedBy()),
                task.getLabels().stream()
                        .map(LabelMapper::toDto)
                        .collect(Collectors.toSet()),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }
}
