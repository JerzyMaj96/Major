package com.jerzymaj.major.services;

import com.jerzymaj.major.repos.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebhookEventService {

    private final WebhookEventRepository webhookEventRepository;

    public void processEvent(String eventType, String payload) {

    }
}
