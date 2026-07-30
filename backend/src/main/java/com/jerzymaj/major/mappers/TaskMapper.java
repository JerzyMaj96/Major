package com.jerzymaj.major.mappers;

import com.jerzymaj.major.Dtos.TaskDto;
import com.jerzymaj.major.models.Task;

public final class TaskMapper {

    private TaskMapper() {}

    public static TaskDto toDto(Task task) {
        return new TaskDto();
    }
}
