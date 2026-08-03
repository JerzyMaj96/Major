package com.jerzymaj.major.services;

import com.jerzymaj.major.Dtos.CreateActivityLogDto;
import com.jerzymaj.major.exceptions.TaskNotFoundException;
import com.jerzymaj.major.models.ActivityLog;
import com.jerzymaj.major.models.Task;
import com.jerzymaj.major.repos.ActivityLogRepository;
import com.jerzymaj.major.repos.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final TaskRepository taskRepository;

    public List<ActivityLog> getAllActivityLogs() {
        return activityLogRepository.findAll();
    }

    public ActivityLog createActivityLog(CreateActivityLogDto createActivityLogDto) {
        Task task = taskRepository.findById(createActivityLogDto.taskId())
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + createActivityLogDto.taskId()));

        return activityLogRepository.save(ActivityLog.builder()
                .changeType(createActivityLogDto.changeType())
                .oldValue(createActivityLogDto.oldValue())
                .newValue(createActivityLogDto.newValue())
                .changedBy(createActivityLogDto.changedBy())
                .task(task)
                .build());
    }

}
