package com.jerzymaj.major.controllers;

import com.jerzymaj.major.configuration.ApiRoutes;
import com.jerzymaj.major.security.GitHubSignatureVerifier;
import com.jerzymaj.major.services.WebhookEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WebhookEventController {

    private final GitHubSignatureVerifier signatureVerifier;
    private final WebhookEventService webhookEventService;

    @Value("${app.github.webhook-secret}")
    private String githubWebhookSecret;

    @PostMapping("/webhook/github")
    public ResponseEntity<String> handleGitHubWebhook(@RequestHeader("X-Hub-Signature-256") String signature,
                                                      @RequestHeader("X-GitHub-Event") String eventType,
                                                      @RequestBody String payload) {

        if (!signatureVerifier.isValidSignature(payload, signature, githubWebhookSecret)) {
            return ResponseEntity.status(401).body("Invalid signature");
        }

        webhookEventService.processEvent(eventType, payload);

        return ResponseEntity.ok("Webhook received");
    }

    @PostMapping(ApiRoutes.BASE_API + "/webhook-events/{webhookEventId}/retry")
    public ResponseEntity<Void> retryHandleGitHubWebhook(@PathVariable Long webhookEventId) {

        webhookEventService.retryProcessWebhookEvent(webhookEventId);

        return ResponseEntity.noContent().build();
    }
}
