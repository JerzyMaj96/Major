package com.jerzymaj.major.unit_tests;

import com.jerzymaj.major.exceptions.AiGenerationException;
import com.jerzymaj.major.exceptions.NoActivityLogsException;
import com.jerzymaj.major.models.ActivityLog;
import com.jerzymaj.major.models.WeeklySummary;
import com.jerzymaj.major.models.enums.ChangeType;
import com.jerzymaj.major.repos.WeeklySummaryRepository;
import com.jerzymaj.major.services.ActivityLogService;
import com.jerzymaj.major.services.GptService;
import com.jerzymaj.major.services.WeeklySummaryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WeeklySummaryServiceUnitTests {

    @Mock
    private WeeklySummaryRepository weeklySummaryRepository;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private GptService gptService;

    @InjectMocks
    private WeeklySummaryService weeklySummaryService;

    @Test
    public void shouldGenerateWeeklySummary() {
        ActivityLog activityLog1 = ActivityLog.builder()
                .changeType(ChangeType.STATUS_CHANGE)
                .newValue("DONE")
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();
        ActivityLog activityLog2 = ActivityLog.builder()
                .changeType(ChangeType.TASK_CHANGE)
                .newValue("Task created")
                .createdAt(LocalDateTime.now().minusDays(4))
                .build();

        List<ActivityLog> activityLogs = List.of(activityLog1, activityLog2);

        when(activityLogService.getAllActivityLogsLastWeek()).thenReturn(activityLogs);
        when(gptService.generateWeeklySummary(activityLogs)).thenReturn(CompletableFuture.completedFuture("Test summary"));
        when(weeklySummaryRepository.save(any(WeeklySummary.class))).thenAnswer(i -> i.getArguments()[0]);

        CompletableFuture<WeeklySummary> actualResult = weeklySummaryService.generateWeeklySummary();

        assertThat(actualResult).isNotNull();
        assertThat(actualResult.join().getContent()).isEqualTo("Test summary");
        assertThat(actualResult.join().getTasksCreated()).isEqualTo(1L);
        assertThat(actualResult.join().getTasksCompleted()).isEqualTo(1L);
    }

    @Test
    public void failedFutureShouldBeReturned_WhenGptServiceFails() {
        ActivityLog activityLog = ActivityLog.builder()
                .changeType(ChangeType.STATUS_CHANGE)
                .newValue("DONE")
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();

        List<ActivityLog> activityLogs = List.of(activityLog);
        when(activityLogService.getAllActivityLogsLastWeek()).thenReturn(activityLogs);
        when(gptService.generateWeeklySummary(activityLogs)).thenReturn(CompletableFuture
                .failedFuture(new AiGenerationException("Failed to generate weekly summary",
                        new RuntimeException("OpenAI unavailable"))));

        CompletableFuture<WeeklySummary> actualResult = weeklySummaryService.generateWeeklySummary();

        assertThat(actualResult.isCompletedExceptionally()).isTrue();

        CompletionException exception = assertThrows(CompletionException.class, actualResult::join);
        assertThat(exception.getCause()).isInstanceOf(AiGenerationException.class);
        assertThat(exception.getCause().getMessage()).isEqualTo("Failed to generate weekly summary");

        verify(weeklySummaryRepository, never()).save(any(WeeklySummary.class));
    }

    @Test
    public void shouldThrowException_WhenNoActivityLogs() {
        when(activityLogService.getAllActivityLogsLastWeek()).thenReturn(List.of());

        assertThrows(NoActivityLogsException.class,
                () -> weeklySummaryService.generateWeeklySummary());
    }
}
