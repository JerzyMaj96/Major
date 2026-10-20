package com.jerzymaj.major.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jerzymaj.major.Dtos.GitHubPullRequestPayload;
import com.jerzymaj.major.Dtos.GitHubPushPayload;
import com.jerzymaj.major.exceptions.TaskNotFoundException;
import com.jerzymaj.major.exceptions.WebhookEventNotFoundException;
import com.jerzymaj.major.models.Task;
import com.jerzymaj.major.models.WebhookEvent;
import com.jerzymaj.major.models.enums.EventType;
import com.jerzymaj.major.models.enums.TaskStatus;
import com.jerzymaj.major.models.enums.WebhookEventStatus;
import com.jerzymaj.major.repos.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes GitHub webhook events and keeps a record of every one of them.
 *
 * <p>A task is linked to a branch by a {@code task-<id>} fragment in the branch name
 * (for example {@code feature/task-42-login}). A push moves the task to {@code IN_PROGRESS},
 * an opened pull request to {@code IN_REVIEW} and a merged one to {@code DONE}.
 * Events that cannot be processed are stored with status {@code FAILED}
 * (a simple dead letter queue) so they can be retried later.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookEventService {

    private final WebhookEventRepository webhookEventRepository;
    private final TaskService taskService;

    /**
     * Handles a single webhook delivery and stores a {@link WebhookEvent} describing the result.
     *
     * <p>Processing errors do not propagate to the caller: an exception (invalid JSON, missing
     * task id, unknown task) is caught, logged and saved as a {@code FAILED} event, so a
     * correctly signed request normally gets a successful response. Event types other than
     * {@code push} and {@code pull_request} are ignored.
     *
     * @param eventType value of the {@code X-GitHub-Event} header
     * @param payload   raw JSON body of the request
     * @return {@code true} if the event was processed or its type is ignored,
     *         {@code false} if processing failed and a {@code FAILED} event was saved
     */
    public boolean processEvent(String eventType, String payload) {
        ObjectMapper objectMapper = new ObjectMapper();

        if (eventType.equals("push")) {
            try {
                GitHubPushPayload pushPayload = objectMapper.readValue(payload, GitHubPushPayload.class);

                Long taskId = extractTaskIdFromRef(pushPayload.ref())
                        .orElseThrow(() -> new TaskNotFoundException("Push ref doesn't contain task id"));

                Task task = changeTaskStatus(taskId, TaskStatus.IN_PROGRESS);

                saveWebhookEvent(payload, EventType.PUSH, WebhookEventStatus.PROCESSED, task, null);

                return true;

            } catch (Exception ex) {
                saveWebhookEvent(payload, EventType.PUSH, WebhookEventStatus.FAILED, null, ex.getMessage());
                log.error("Failed to process push webhook event", ex);

                return false;
            }
        }

        if (eventType.equals("pull_request")) {
            try {
                GitHubPullRequestPayload pullRequestPayload = objectMapper.readValue(payload, GitHubPullRequestPayload.class);

                Long taskId = extractTaskIdFromRef(pullRequestPayload.pullRequest().head().ref())
                        .orElseThrow(() -> new TaskNotFoundException("Pull request head's ref doesn't contain task id"));

                processPullRequestByAction(payload, pullRequestPayload, taskId);

                return true;

            } catch (Exception ex) {
                saveWebhookEvent(payload, EventType.PULL_REQUEST_UNKNOWN, WebhookEventStatus.FAILED, null, ex.getMessage());
                log.error("Failed to process pull_request webhook event", ex);

                return false;
            }
        }
        return true;
    }

    /**
     * Re-runs processing of a previously failed event using its stored payload.
     *
     * <p>The retry goes through {@link #processEvent(String, String)}, which always saves a
     * <em>new</em> event with the result. If the retry succeeds, the original event is also set to
     * {@code PROCESSED} and its error message is cleared, so it leaves the failed list. If the retry
     * fails, the original stays {@code FAILED} and one more {@code FAILED} event is saved.
     *
     * @param webhookEventId id of the failed event
     * @throws WebhookEventNotFoundException if no event with this id exists
     * @throws IllegalStateException         if the event is not in status {@code FAILED}
     */
    public void retryProcessWebhookEvent(Long webhookEventId) {
        WebhookEvent webhookEvent = webhookEventRepository.findById(webhookEventId)
                .orElseThrow(() -> new WebhookEventNotFoundException("Webhook event not found with id: " + webhookEventId));

        if (webhookEvent.getStatus() != WebhookEventStatus.FAILED) {
            throw new IllegalStateException("Only failed events can be retried");
        }

        String rawEventType = mapEventTypeToRawString(webhookEvent.getEventType());

        boolean success = processEvent(rawEventType, webhookEvent.getPayload());

        if (success) {
            webhookEvent.setStatus(WebhookEventStatus.PROCESSED);
            webhookEvent.setErrorMessage(null);
            webhookEventRepository.save(webhookEvent);
        }
    }

    public List<WebhookEvent> getWebhookEventByStatus(WebhookEventStatus status) {
        return webhookEventRepository.findByStatusDesc(status);
    }

    private String mapEventTypeToRawString(EventType eventType) {
        return switch (eventType) {
            case PUSH -> "push";
            case PULL_REQUEST_OPENED, PULL_REQUEST_MERGED, PULL_REQUEST_CLOSED, PULL_REQUEST_OTHER,
                 PULL_REQUEST_UNKNOWN -> "pull_request";
        };
    }

    /**
     * Extracts the task id from a branch ref, e.g. {@code refs/heads/feature/task-42-login} gives 42.
     *
     * @return the id, or an empty {@code Optional} if the ref contains no {@code task-<digits>} part
     */
    private Optional<Long> extractTaskIdFromRef(String ref) {
        Pattern pattern = Pattern.compile("task-(\\d+)");
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
        return taskService.updateTaskStatus(taskId, status, "GitHub webhook");
    }

    private void processPullRequestByAction(String payload, GitHubPullRequestPayload pullRequestPayload, Long taskId) {
        switch (pullRequestPayload.action()) {
            case "opened" -> {
                Task task = changeTaskStatus(taskId, TaskStatus.IN_REVIEW);
                saveWebhookEvent(payload, EventType.PULL_REQUEST_OPENED, WebhookEventStatus.PROCESSED, task, null);
            }
            case "closed" -> {
                if (pullRequestPayload.pullRequest().merged()) {
                    Task task = changeTaskStatus(taskId, TaskStatus.DONE);
                    saveWebhookEvent(payload, EventType.PULL_REQUEST_MERGED, WebhookEventStatus.PROCESSED, task, null);
                } else {
                    log.info("Pull request closed without merge, task status unchanged");
                    saveWebhookEvent(payload, EventType.PULL_REQUEST_CLOSED, WebhookEventStatus.PROCESSED, null, null);
                }
            }
            default -> {
                log.info("Unhandled pull request action: {}", pullRequestPayload.action());
                saveWebhookEvent(payload, EventType.PULL_REQUEST_OTHER, WebhookEventStatus.PROCESSED, null, null);
            }
        }
    }
}
