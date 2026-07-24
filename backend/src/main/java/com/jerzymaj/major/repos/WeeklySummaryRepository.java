package com.jerzymaj.major.repos;

import com.jerzymaj.major.models.WeeklySummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklySummaryRepository extends JpaRepository<WeeklySummary, Long> {
}
