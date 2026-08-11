package com.jerzymaj.major.services;

import com.jerzymaj.major.exceptions.NoActivityLogsException;
import com.jerzymaj.major.exceptions.WeekSummaryNotFoundException;
import com.jerzymaj.major.models.ActivityLog;
import com.jerzymaj.major.models.WeeklySummary;
import com.jerzymaj.major.models.enums.ChangeType;
import com.jerzymaj.major.repos.WeeklySummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklySummaryService {

    private final WeeklySummaryRepository weeklySummaryRepository;
    private final ActivityLogService activityLogService;
    private final GptService gptService;

    @Scheduled(cron = "0 0 9 * * MON")
    public void scheduleWeeklySummaryGeneration() {
        try {
            generateWeeklySummary();
        } catch (NoActivityLogsException ex) {
            log.info("No activity logs found for the past week, skipping summary generation");
        }
    }

    public WeeklySummary generateWeeklySummary() {
        List<ActivityLog> activityLogs = activityLogService.getAllActivityLogsLastWeek();

        if (activityLogs.isEmpty()) {
            throw new NoActivityLogsException("No activity logs found for the last week");
        }

        Map<String, Long> weeklyStats = getWeeklyStats(activityLogs);

        String summaryText = gptService.generateWeeklySummary(activityLogs);

        WeeklySummary weeklySummary = WeeklySummary.builder()
                .content(summaryText)
                .tasksCompleted(weeklyStats.get("completed"))
                .tasksCreated(weeklyStats.get("created"))
                .periodStart(LocalDate.from(activityLogs.stream()
                        .map(ActivityLog::getCreatedAt)
                        .min(LocalDateTime::compareTo)
                        .orElseThrow()))
                .periodEnd(LocalDate.from(activityLogs.stream()
                        .map(ActivityLog::getCreatedAt)
                        .max(LocalDateTime::compareTo)
                        .orElseThrow()))
                .build();

        return weeklySummaryRepository.save(weeklySummary);
    }

    public WeeklySummary getWeeklySummary() {

        return weeklySummaryRepository.findFirstByOrderByGeneratedAtDesc()
                .orElseThrow(() -> new WeekSummaryNotFoundException("No weekly summary found"));
    }

    private Map<String, Long> getWeeklyStats(List<ActivityLog> activityLogs) {

        long tasksCompleted = activityLogs.stream()
                .filter(a -> a.getChangeType() == ChangeType.STATUS_CHANGE &&
                        a.getNewValue().equals("DONE"))
                .count();

        long tasksCreated = activityLogs.stream()
                .filter(a -> a.getChangeType() == ChangeType.TASK_CHANGE &&
                        a.getNewValue().equals("Task created"))
                .count();

        Map<String, Long> weeklyStats = new HashMap<>();
        weeklyStats.put("completed", tasksCompleted);
        weeklyStats.put("created", tasksCreated);

        return weeklyStats;
    }

}
