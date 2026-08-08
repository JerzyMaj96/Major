package com.jerzymaj.major.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jerzymaj.major.Dtos.GitHubPullRequestPayload;
import com.jerzymaj.major.Dtos.GitHubPushPayload;
import com.jerzymaj.major.exceptions.TaskNotFoundException;
import com.jerzymaj.major.models.Task;
import com.jerzymaj.major.models.WebhookEvent;
import com.jerzymaj.major.models.enums.EventType;
import com.jerzymaj.major.models.enums.TaskStatus;
import com.jerzymaj.major.models.enums.WebhookEventStatus;
import com.jerzymaj.major.repos.TaskRepository;
import com.jerzymaj.major.repos.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookEventService {

    private final WebhookEventRepository webhookEventRepository;
    private final TaskRepository taskRepository;
    private final TaskService taskService;

    public void processEvent(String eventType, String payload) {
        ObjectMapper objectMapper = new ObjectMapper();

        if (eventType.equals("push")) {
            try {
                GitHubPushPayload pushPayload = objectMapper.readValue(payload, GitHubPushPayload.class);

                Long taskId = extractTaskIdFromRef(pushPayload.ref())
                        .orElseThrow(() -> new TaskNotFoundException("Push ref doesn't contain task id"));

                Task task = changeTaskStatus(taskId, TaskStatus.IN_PROGRESS);

                saveWebhookEvent(payload, EventType.PUSH, WebhookEventStatus.PROCESSED, task, null);

            } catch (Exception ex) {
                saveWebhookEvent(payload, EventType.PUSH, WebhookEventStatus.FAILED, null, ex.getMessage());
                log.error("Failed to process push webhook event", ex);
            }
        }

        if (eventType.equals("pull_request")) {
            try {
                GitHubPullRequestPayload pullRequestPayload = objectMapper.readValue(payload, GitHubPullRequestPayload.class);

                Long taskId = extractTaskIdFromRef(pullRequestPayload.pullRequest().head().ref())
                        .orElseThrow(() -> new TaskNotFoundException("Pull request head's ref doesn't contain task id"));

                processPullRequestByAction(payload, pullRequestPayload, taskId);

            } catch (Exception ex) {
                saveWebhookEvent(payload, EventType.PULL_REQUEST_UNKNOWN, WebhookEventStatus.FAILED, null, ex.getMessage());
                log.error("Failed to process pull_request webhook event", ex);
            }
        }
    }

    private Optional<Long> extractTaskIdFromRef(String ref) {
        Pattern pattern = Pattern.compile("task-/(\\d+)");
        Matcher matcher = pattern.matcher(ref);

        if (matcher.find()) {
            return Optional.of(Long.parseLong(matcher.group(1)));
        }

        return Optional.empty();
    }

    private void saveWebhookEvent(String payload, EventType eventType, WebhookEventStatus status, Task task, String errorMessage) {
        WebhookEvent webhookEvent = WebhookEvent.builder()
                .eventType(eventType)
                .payload(payload)
                .task(task)
                .status(status)
                .errorMessage(errorMessage)
                .build();

        webhookEventRepository.save(webhookEvent);
    }

    private Task changeTaskStatus(Long taskId, TaskStatus status) {
        Task task = taskService.getTaskById(taskId);
        task.setStatus(status);
        return taskRepository.save(task);
    }

    private void processPullRequestByAction(String payload, GitHubPullRequestPayload pullRequestPayload, Long taskId) {
        if (pullRequestPayload.action().equals("opened")) {
            Task task = changeTaskStatus(taskId, TaskStatus.IN_REVIEW);
            saveWebhookEvent(payload, EventType.PULL_REQUEST_OPENED, WebhookEventStatus.PROCESSED, task, null);

        } else if (pullRequestPayload.action().equals("closed") && pullRequestPayload.pullRequest().merged()) {
            Task task = changeTaskStatus(taskId, TaskStatus.DONE);
            saveWebhookEvent(payload, EventType.PULL_REQUEST_MERGED, WebhookEventStatus.PROCESSED, task, null);

        } else if (pullRequestPayload.action().equals("closed")) {
            log.info("Pull request closed without merge, task status unchanged");
            saveWebhookEvent(payload, EventType.PULL_REQUEST_CLOSED, WebhookEventStatus.PROCESSED, null, null);

        } else {
            log.info("Unhandled pull request action: {}", pullRequestPayload.action());
            saveWebhookEvent(payload, EventType.PULL_REQUEST_OTHER, WebhookEventStatus.PROCESSED, null, null);
        }
    }
}
