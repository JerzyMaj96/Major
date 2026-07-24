package com.jerzymaj.major.repos;

import com.jerzymaj.major.models.Label;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabelRepository extends JpaRepository<Label, Long> {
}
