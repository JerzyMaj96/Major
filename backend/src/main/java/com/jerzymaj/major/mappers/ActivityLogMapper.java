package com.jerzymaj.major.mappers;

import com.jerzymaj.major.Dtos.ActivityLogDto;
import com.jerzymaj.major.models.ActivityLog;

public final class ActivityLogMapper {

    private  ActivityLogMapper() {
    }

    public static ActivityLogDto toDto(ActivityLog activityLog) {
        return new ActivityLogDto(
                activityLog.getId(),
                activityLog.getChangeType(),
                activityLog.getOldValue(),
                activityLog.getNewValue(),
                activityLog.getCreatedAt(),
                activityLog.getChangedBy()
        );
    }
}
