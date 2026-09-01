package com.jerzymaj.major.controllers;

import com.jerzymaj.major.Dtos.WebhookEventDto;
import com.jerzymaj.major.configuration.ApiRoutes;
import com.jerzymaj.major.mappers.WebhookEventMapper;
import com.jerzymaj.major.models.enums.WebhookEventStatus;
import com.jerzymaj.major.services.WebhookEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WebhookEventController {

    private final WebhookEventService webhookEventService;

    @PostMapping(ApiRoutes.BASE_API + "/webhook-events/{webhookEventId}/retry")
    public ResponseEntity<Void> retryHandleGitHubWebhook(@PathVariable Long webhookEventId) {

        webhookEventService.retryProcessWebhookEvent(webhookEventId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping(ApiRoutes.BASE_API + "/webhook-events")
    public ResponseEntity<List<WebhookEventDto>> retrieveWebhookEventsByStatus(@RequestParam("status") WebhookEventStatus webhookEventStatus) {

        return ResponseEntity.ok(webhookEventService.getWebhookEventByStatus(webhookEventStatus).stream()
                .map(WebhookEventMapper::toDto)
                .toList());
    }
}
