package com.jerzymaj.major.integration_tests;

import com.jerzymaj.major.configuration.WithMockCustomUser;
import com.jerzymaj.major.models.Task;
import com.jerzymaj.major.models.User;
import com.jerzymaj.major.models.WebhookEvent;
import com.jerzymaj.major.models.enums.TaskStatus;
import com.jerzymaj.major.models.enums.WebhookEventStatus;
import com.jerzymaj.major.repos.TaskRepository;
import com.jerzymaj.major.repos.UserRepository;
import com.jerzymaj.major.repos.WebhookEventRepository;
import com.jerzymaj.major.security.GitHubSignatureVerifier;
import com.jerzymaj.major.services.GptService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class WebhookEventControllerIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GitHubSignatureVerifier signatureVerifier;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private WebhookEventRepository webhookEventRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.github.webhook-secret}")
    private String githubWebhookSecret;

    @MockitoBean
    private GptService gptService;

    private Task testTask;

    @BeforeEach
    void setup() {
        User creator = userRepository.findByName("tester")
                .orElseThrow(() -> new IllegalStateException("Expected test user not found"));

        testTask = Task.builder()
                .title("Test Task")
                .status(TaskStatus.BACKLOG)
                .createdBy(creator)
                .build();
        taskRepository.save(testTask);
    }

    @Test
    @WithMockCustomUser
    public void handleGitHubWebhook_Push() throws Exception {
        String payload = """
                {
                    "ref": "refs/heads/task-%d"
                }
                """.formatted(testTask.getId());

        String signature = "sha256=" + signatureVerifier.computeHmac(payload, githubWebhookSecret);

        mockMvc.perform(post("/webhook/github")
                        .headers(new HttpHeaders() {{
                            add("X-Hub-Signature-256", signature);
                            add("X-GitHub-Event", "push");
                        }})
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        Task updatedTask = taskRepository.findById(testTask.getId()).orElseThrow();
        assertThat(updatedTask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);

        List<WebhookEvent> events = webhookEventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getStatus()).isEqualTo(WebhookEventStatus.PROCESSED);
        assertThat(events.get(0).getEventType().name()).isEqualTo("PUSH");

    }

    @Test
    @WithMockCustomUser
    public void  handleGitHubWebhook_PushFailed() throws Exception {

        String payload = "Invalid JSON Payload";

        String signature = "sha256=" + signatureVerifier.computeHmac(payload, githubWebhookSecret);

        mockMvc.perform(post("/webhook/github")
                        .headers(new HttpHeaders() {{
                            add("X-Hub-Signature-256", signature);
                            add("X-GitHub-Event", "push");
                        }})
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        List<WebhookEvent> events = webhookEventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getStatus()).isEqualTo(WebhookEventStatus.FAILED);
        assertThat(events.get(0).getEventType().name()).isEqualTo("PUSH");
    }

    @Test
    @WithMockCustomUser
    public void handleGitHubWebhook_PullRequest_OpenedNotMerged() throws Exception {

        String payload = """
                {
                  "action": "opened",
                  "pull_request": {
                    "head": {
                      "ref": "refs/heads/task-%d"
                    },
                    "merged": false
                  }
                }
                """.formatted(testTask.getId());

        String signature = "sha256=" + signatureVerifier.computeHmac(payload, githubWebhookSecret);

        mockMvc.perform(post("/webhook/github")
                        .headers(new HttpHeaders() {{
                            add("X-Hub-Signature-256", signature);
                            add("X-GitHub-Event", "pull_request");
                        }})
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        Task updatedTask = taskRepository.findById(testTask.getId()).orElseThrow();
        assertThat(updatedTask.getStatus()).isEqualTo(TaskStatus.IN_REVIEW);

        List<WebhookEvent> events = webhookEventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getStatus()).isEqualTo(WebhookEventStatus.PROCESSED);
        assertThat(events.get(0).getEventType().name()).isEqualTo("PULL_REQUEST_OPENED");
    }

    @Test
    @WithMockCustomUser
    public void handleGitHubWebhook_PullRequest_ClosedAndMerged() throws Exception {

        String payload = """
                {
                  "action": "closed",
                  "pull_request": {
                    "head": {
                      "ref": "refs/heads/task-%d"
                    },
                    "merged": true
                  }
                }
                """.formatted(testTask.getId());

        String signature = "sha256=" + signatureVerifier.computeHmac(payload, githubWebhookSecret);

        mockMvc.perform(post("/webhook/github")
                        .headers(new HttpHeaders() {{
                            add("X-Hub-Signature-256", signature);
                            add("X-GitHub-Event", "pull_request");
                        }})
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        Task updatedTask = taskRepository.findById(testTask.getId()).orElseThrow();
        assertThat(updatedTask.getStatus()).isEqualTo(TaskStatus.DONE);

        List<WebhookEvent> events = webhookEventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getStatus()).isEqualTo(WebhookEventStatus.PROCESSED);
        assertThat(events.get(0).getEventType().name()).isEqualTo("PULL_REQUEST_MERGED");
    }

    @Test
    @WithMockCustomUser
    public void handleGitHubWebhook_PullRequest_ClosedNotMerged() throws Exception {

        String payload = """
                {
                  "action": "closed",
                  "pull_request": {
                    "head": {
                      "ref": "refs/heads/task-%d"
                    },
                    "merged": false
                  }
                }
                """.formatted(testTask.getId());

        String signature = "sha256=" + signatureVerifier.computeHmac(payload, githubWebhookSecret);

        mockMvc.perform(post("/webhook/github")
                        .headers(new HttpHeaders() {{
                            add("X-Hub-Signature-256", signature);
                            add("X-GitHub-Event", "pull_request");
                        }})
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        List<WebhookEvent> events = webhookEventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getStatus()).isEqualTo(WebhookEventStatus.PROCESSED);
        assertThat(events.get(0).getEventType().name()).isEqualTo("PULL_REQUEST_CLOSED");
    }

    @Test
    @WithMockCustomUser
    public void handleGitHubWebhook_UnhandledPullRequest() throws Exception {

        String payload = """
                {
                  "action": "unknown_action",
                  "pull_request": {
                    "head": {
                      "ref": "refs/heads/task-%d"
                    },
                    "merged": false
                  }
                }
                """.formatted(testTask.getId());

        String signature = "sha256=" + signatureVerifier.computeHmac(payload, githubWebhookSecret);

        mockMvc.perform(post("/webhook/github")
                        .headers(new HttpHeaders() {{
                            add("X-Hub-Signature-256", signature);
                            add("X-GitHub-Event", "pull_request");
                        }})
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        List<WebhookEvent> events = webhookEventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getStatus()).isEqualTo(WebhookEventStatus.PROCESSED);
        assertThat(events.get(0).getEventType().name()).isEqualTo("PULL_REQUEST_OTHER");
    }

    @Test
    @WithMockCustomUser
    public void handleGitHubWebhook_PullRequest_InvalidRef() throws Exception {

        String payload = "Invalid JSON Payload";

        String signature = "sha256=" + signatureVerifier.computeHmac(payload, githubWebhookSecret);

        mockMvc.perform(post("/webhook/github")
                        .headers(new HttpHeaders() {{
                            add("X-Hub-Signature-256", signature);
                            add("X-GitHub-Event", "pull_request");
                        }})
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        List<WebhookEvent> events = webhookEventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getStatus()).isEqualTo(WebhookEventStatus.FAILED);
        assertThat(events.get(0).getEventType().name()).isEqualTo("PULL_REQUEST_UNKNOWN");
    }
}
