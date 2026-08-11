package com.jerzymaj.major.mappers;

import com.jerzymaj.major.Dtos.WeeklySummaryDto;
import com.jerzymaj.major.models.WeeklySummary;

public final class WeeklySummaryMapper {

    private WeeklySummaryMapper() {
    }

    public static WeeklySummaryDto toDto(WeeklySummary weeklySummary) {

        return new WeeklySummaryDto(
          weeklySummary.getId(),
          weeklySummary.getContent(),
          weeklySummary.getTasksCompleted(),
          weeklySummary.getTasksCreated(),
          weeklySummary.getPeriodStart(),
          weeklySummary.getPeriodEnd(),
          weeklySummary.getGeneratedAt()
        );
    }
}
