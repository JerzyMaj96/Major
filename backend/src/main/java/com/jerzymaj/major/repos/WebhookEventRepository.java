package com.jerzymaj.major.repos;

import com.jerzymaj.major.models.WebhookEvent;
import com.jerzymaj.major.models.enums.WebhookEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    List<WebhookEvent> findByStatus(WebhookEventStatus status);
}
