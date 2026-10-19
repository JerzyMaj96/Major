package com.jerzymaj.major.services;

import com.jerzymaj.major.exceptions.AiGenerationException;
import com.jerzymaj.major.exceptions.NoActivityLogsException;
import com.jerzymaj.major.exceptions.WeeklySummaryNotFoundException;
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
import java.util.concurrent.CompletableFuture;


/**
 * Creates and reads AI-generated weekly summaries of the team's activity.
 *
 * <p>Summaries are built from {@link ActivityLog} entries of the last 7 days. They are
 * generated automatically every Monday and on demand through the REST API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklySummaryService {

    private final WeeklySummaryRepository weeklySummaryRepository;
    private final ActivityLogService activityLogService;
    private final GptService gptService;

    /**
     * Generates a summary every Monday at 09:00. It only fires while the application is running
     * (missed runs are not caught up), which is why an on-demand endpoint exists as well.
     * A week without activity is skipped and logged at info level.
     */
    @Scheduled(cron = "0 0 9 * * MON")
    public void scheduleWeeklySummaryGeneration() {
        try {
            generateWeeklySummary()
                    .exceptionally(ex -> {
                        log.error("Failed to generate weekly summary", ex);
                        return null;
                    });
        } catch (NoActivityLogsException ex) {
            log.info("No activity logs found for the past week, skipping summary generation");
        }
    }

    /**
     * Generates and saves a summary of the last 7 days of activity.
     *
     * <p>The period start and end are taken from the earliest and latest log entry, task counts
     * (created and completed) are computed locally, and the text is written by the AI model
     * asynchronously (see {@link GptService#generateWeeklySummary(List)}).
     *
     * @return a future completing with the saved summary; if the AI call fails, it completes
     *         exceptionally (the cause is {@link AiGenerationException}, usually wrapped in a
     *         {@code CompletionException})
     * @throws NoActivityLogsException synchronously, when there is no activity in the last 7 days
     */
    public CompletableFuture<WeeklySummary> generateWeeklySummary() {
        List<ActivityLog> activityLogs = activityLogService.getAllActivityLogsLastWeek();

        if (activityLogs.isEmpty()) {
            throw new NoActivityLogsException("No activity logs found for the last week");
        }

        Map<String, Long> weeklyStats = getWeeklyStats(activityLogs);

        LocalDate periodStart = LocalDate.from(activityLogs.stream()
                .map(ActivityLog::getCreatedAt)
                .min(LocalDateTime::compareTo)
                .orElseThrow());

        LocalDate periodEnd = LocalDate.from(activityLogs.stream()
                .map(ActivityLog::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElseThrow());

        return gptService.generateWeeklySummary(activityLogs)
                .thenApply(summaryText -> weeklySummaryRepository.save(
                        WeeklySummary.builder()
                                .content(summaryText)
                                .tasksCompleted(weeklyStats.get("completed"))
                                .tasksCreated(weeklyStats.get("created"))
                                .periodStart(periodStart)
                                .periodEnd(periodEnd)
                                .build()));
    }

    /**
     * Returns the most recent summary (the one with the latest period end).
     *
     * @throws WeeklySummaryNotFoundException if no summary has been generated yet
     */
    public WeeklySummary getLastWeeksSummary() {

        return weeklySummaryRepository.findFirstByOrderByPeriodEndDesc()
                .orElseThrow(() -> new WeeklySummaryNotFoundException("No weekly summary found"));
    }

    public List<WeeklySummary> getAllWeeklySummary() {
        return weeklySummaryRepository.findAll();
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
