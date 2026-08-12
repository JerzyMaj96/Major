package com.jerzymaj.major.mappers;

import com.jerzymaj.major.Dtos.WebhookEventDto;
import com.jerzymaj.major.models.WebhookEvent;

public final class WebhookEventMapper {

    private WebhookEventMapper() {
    }

    public static WebhookEventDto toDto(WebhookEvent webhookEvent) {
        Long taskId = webhookEvent.getTask() != null ? webhookEvent.getTask().getId() : null;

        return new WebhookEventDto(
                webhookEvent.getId(),
                webhookEvent.getEventType(),
                webhookEvent.getPayload(),
                webhookEvent.getReceivedAt(),
                webhookEvent.getStatus(),
                webhookEvent.getErrorMessage(),
                taskId
        );
    }
}
