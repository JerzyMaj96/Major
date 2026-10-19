package com.jerzymaj.major.controllers;

import com.jerzymaj.major.security.GitHubSignatureVerifier;
import com.jerzymaj.major.services.WebhookEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoint that receives webhooks from GitHub. It is not protected by JWT; requests are
 * authenticated by verifying the HMAC signature instead.
 */
@RestController
@RequiredArgsConstructor
public class GitHubWebhookController {

    private final WebhookEventService webhookEventService;
    private final GitHubSignatureVerifier signatureVerifier;

    @Value("${app.github.webhook-secret}")
    private String githubWebhookSecret;

    /**
     * Verifies the signature and hands the event over for processing.
     *
     * @return 401 if the signature is invalid, otherwise 200 (Spring itself answers 400 when a
     *         required header is missing)
     */
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
}
