package com.jerzymaj.major.repos;

import com.jerzymaj.major.models.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
}
