package com.jerzymaj.major.repos;

import com.jerzymaj.major.models.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
}
