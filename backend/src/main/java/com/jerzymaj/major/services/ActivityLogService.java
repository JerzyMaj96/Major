package com.jerzymaj.major.services;

import com.jerzymaj.major.models.ActivityLog;
import com.jerzymaj.major.models.Task;
import com.jerzymaj.major.models.enums.ChangeType;
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

    public List<ActivityLog> findAllActivityLogs() {
        return activityLogRepository.findAll();
    }

    public List<ActivityLog> getAllActivityLogsForTask(Long taskId) {
        return activityLogRepository.findAllByTaskId(taskId);
    }

    public ActivityLog createActivityLog(Task task, ChangeType changeType, String oldValue, String newValue, String changedBy) {

        return activityLogRepository.save(ActivityLog.builder()
                .changeType(changeType)
                .oldValue(oldValue)
                .newValue(newValue)
                .changedBy(changedBy)
                .task(task)
                .build());
    }


}
