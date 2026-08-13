package com.jerzymaj.major.Dtos;

import com.jerzymaj.major.models.enums.EventType;
import com.jerzymaj.major.models.enums.WebhookEventStatus;

import java.time.LocalDateTime;

public record WebhookEventDto(Long id, EventType eventType, String payload, LocalDateTime receivedAt,
                              WebhookEventStatus status, String errorMessage, Long taskId) {
}
