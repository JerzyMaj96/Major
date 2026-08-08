package com.jerzymaj.major.services;

import com.jerzymaj.major.repos.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class WebhookEventService {

    private final WebhookEventRepository webhookEventRepository;

    private Optional<Long> extractTaskIdFromRef(String ref) {
        Pattern pattern = Pattern.compile("task/(\\d+)");
        Matcher matcher = pattern.matcher(ref);

        if (matcher.find()) {
            return Optional.of(Long.parseLong(matcher.group(1)));
        }

        return Optional.empty();
    }

    public void processEvent(String eventType, String payload) {

    }
}
