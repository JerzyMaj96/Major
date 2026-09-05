package com.jerzymaj.major.repos;

import com.jerzymaj.major.models.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findAllByTaskId(Long taskId);

    @Query(
            value = """
                            SELECT a
                            FROM ActivityLog a
                            WHERE a.createdAt >= NOW() - INTERVAL '7 days'
                            ORDER BY a.createdAt DESC
                    """
    , nativeQuery = true)
    List<ActivityLog> findAllLastWeek();
}
