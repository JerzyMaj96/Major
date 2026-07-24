package com.jerzymaj.major.repos;

import com.jerzymaj.major.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
