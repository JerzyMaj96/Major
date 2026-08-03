package com.jerzymaj.major.repos;

import com.jerzymaj.major.models.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findAllByTaskId(Long taskId);
}
