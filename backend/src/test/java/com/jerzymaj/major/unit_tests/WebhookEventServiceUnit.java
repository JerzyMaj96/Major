package com.jerzymaj.major.unit_tests;

import com.jerzymaj.major.models.Task;
import com.jerzymaj.major.models.WebhookEvent;
import com.jerzymaj.major.models.enums.EventType;
import com.jerzymaj.major.models.enums.TaskStatus;
import com.jerzymaj.major.models.enums.WebhookEventStatus;
import com.jerzymaj.major.repos.WebhookEventRepository;
import com.jerzymaj.major.services.TaskService;
import com.jerzymaj.major.services.WebhookEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.setAllowExtractingPrivateFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WebhookEventServiceUnit {

    @Mock
    private WebhookEventRepository webhookEventRepository;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private WebhookEventService webhookEventService;

    private Task testTask;

    @BeforeEach
    public void setup() {
        testTask = Task.builder()
                .id(123L)
                .title("Test Task")
                .status(TaskStatus.BACKLOG)
                .build();

        lenient().when(webhookEventRepository.save(any(WebhookEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        lenient().when(taskService.updateTaskStatus(any(Long.class), any(TaskStatus.class), anyString()))
                .thenReturn(testTask);
    }

    @Test
    public void shouldProcessPushWebhookEvent_IfSuccess() {
        String eventType = "push";
        String payload = """
                {
                "ref": "refs/heads/task-123"
                }
                """;

        webhookEventService.processEvent(eventType, payload);

        verify(webhookEventRepository).save(any(WebhookEvent.class));
        verify(taskService).updateTaskStatus(eq(123L), eq(TaskStatus.IN_PROGRESS), anyString());
    }

    @Test
    public void shouldProcessOpenedPullRequestWebhookEvent_IfSuccess() {
        String eventType = "pull_request";
        String payload = """
                {
                  "action": "opened",
                  "pull_request": {
                    "head": {
                      "ref": "refs/heads/task-123"
                    },
                    "merged": false
                  }
                }
                """;


        webhookEventService.processEvent(eventType, payload);

        ArgumentCaptor<WebhookEvent> webhookEventCaptor = ArgumentCaptor.forClass(WebhookEvent.class);

        verify(webhookEventRepository).save(webhookEventCaptor.capture());
        verify(taskService).updateTaskStatus(eq(123L), eq(TaskStatus.IN_REVIEW), anyString());

        WebhookEvent webhookEvent = webhookEventCaptor.getValue();
        assertThat(webhookEvent.getEventType()).isEqualTo(EventType.PULL_REQUEST_OPENED);
        assertThat(webhookEvent.getStatus()).isEqualTo(WebhookEventStatus.PROCESSED);

    }

    @Test
    public void shouldProcessClosedPullRequestWebhookEventWhenMerged_IfSuccess() {
        String eventType = "pull_request";
        String payload = """
                {
                  "action": "closed",
                  "pull_request": {
                    "head": {
                      "ref": "refs/heads/task-123"
                    },
                    "merged": true
                  }
                }
                """;

        webhookEventService.processEvent(eventType, payload);

        ArgumentCaptor<WebhookEvent> webhookEventCaptor = ArgumentCaptor.forClass(WebhookEvent.class);

        verify(webhookEventRepository).save(webhookEventCaptor.capture());
        verify(taskService).updateTaskStatus(eq(123L), eq(TaskStatus.DONE), anyString());

        WebhookEvent capturedWebhookEvent = webhookEventCaptor.getValue();
        assertThat(capturedWebhookEvent.getEventType()).isEqualTo(EventType.PULL_REQUEST_MERGED);
        assertThat(capturedWebhookEvent.getStatus()).isEqualTo(WebhookEventStatus.PROCESSED);
    }

    @Test
    public void shouldProcessClosedPullRequestWebhookEventWhenNotMerged_IfSuccess() {
        String eventType = "pull_request";
        String payload = """
                {
                  "action": "closed",
                  "pull_request": {
                    "head": {
                      "ref": "refs/heads/task-123"
                    },
                    "merged": false
                  }
                }
                """;

        webhookEventService.processEvent(eventType, payload);

        ArgumentCaptor<WebhookEvent> webhookEventCaptor = ArgumentCaptor.forClass(WebhookEvent.class);

        verify(webhookEventRepository).save(webhookEventCaptor.capture());

        WebhookEvent capturedWebhookEvent = webhookEventCaptor.getValue();
        assertThat(capturedWebhookEvent.getEventType()).isEqualTo(EventType.PULL_REQUEST_CLOSED);
        assertThat(capturedWebhookEvent.getStatus()).isEqualTo(WebhookEventStatus.PROCESSED);
    }

    @Test
    public void shouldHandleInvalidPushPayload_IfExceptionThrown() {
        String eventType = "push";
        String invalidPayload = "{ invalid json }";

        webhookEventService.processEvent(eventType, invalidPayload);

        ArgumentCaptor<WebhookEvent> webhookEventCaptor = ArgumentCaptor.forClass(WebhookEvent.class);
        verify(webhookEventRepository).save(webhookEventCaptor.capture());

        WebhookEvent capturedWebhookEvent = webhookEventCaptor.getValue();
        assertThat(capturedWebhookEvent.getStatus()).isEqualTo(WebhookEventStatus.FAILED);
        assertThat(capturedWebhookEvent.getEventType()).isEqualTo(EventType.PUSH);
        assertThat(capturedWebhookEvent.getErrorMessage()).isNotNull();
    }

    @Test
    public void shouldHandleInvalidPullRequestPayload_IfExceptionThrown() {
        String eventType = "pull_request";
        String invalidPayload = "{ invalid json }";

        webhookEventService.processEvent(eventType, invalidPayload);

        ArgumentCaptor<WebhookEvent> webhookEventCaptor = ArgumentCaptor.forClass(WebhookEvent.class);

        verify(webhookEventRepository).save(webhookEventCaptor.capture());

        WebhookEvent capturedWebhookEvent = webhookEventCaptor.getValue();
        assertThat(capturedWebhookEvent.getStatus()).isEqualTo(WebhookEventStatus.FAILED);
        assertThat(capturedWebhookEvent.getEventType()).isEqualTo(EventType.PULL_REQUEST_UNKNOWN);
        assertThat(capturedWebhookEvent.getErrorMessage()).isNotNull();
    }

    @Test
    public void shouldHandleUnhandledPullRequestPayload_IfExceptionThrown() {
        String eventType = "pull_request";
        String payload = """
                {
                  "action": "unknown_action",
                  "pull_request": {
                    "head": {
                      "ref": "refs/heads/task-123"
                    },
                    "merged": false
                  }
                }
                """;

        webhookEventService.processEvent(eventType, payload);

        ArgumentCaptor<WebhookEvent> webhookEventCaptor = ArgumentCaptor.forClass(WebhookEvent.class);

        verify(webhookEventRepository).save(webhookEventCaptor.capture());

        WebhookEvent capturedWebhookEvent = webhookEventCaptor.getValue();
        assertThat(capturedWebhookEvent.getStatus()).isEqualTo(WebhookEventStatus.PROCESSED);
        assertThat(capturedWebhookEvent.getEventType()).isEqualTo(EventType.PULL_REQUEST_OTHER);

    }

    @Test
    public void shouldGetWebhookEventByStatus_If_Success() {
        WebhookEvent webhookEvent = WebhookEvent.builder()
                .status(WebhookEventStatus.FAILED)
                .errorMessage("test error message")
                .build();

        when(webhookEventRepository.findByStatus(WebhookEventStatus.FAILED)).thenReturn(List.of(webhookEvent));

        List<WebhookEvent> actualResult = webhookEventRepository.findByStatus(WebhookEventStatus.FAILED);

        assertThat(actualResult).isEqualTo(List.of(webhookEvent));
    }
}
