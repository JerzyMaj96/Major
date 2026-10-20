package com.jerzymaj.major.repos;

import com.jerzymaj.major.models.WebhookEvent;
import com.jerzymaj.major.models.enums.WebhookEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    @Query("""
            SELECT we
                FROM WebhookEvent we
                WHERE we.status = :status
                ORDER BY we.receivedAt DESC
            """)
    List<WebhookEvent> findByStatusDesc(@Param("status") WebhookEventStatus status);
}
