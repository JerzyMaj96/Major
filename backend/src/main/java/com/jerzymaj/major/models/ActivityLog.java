package com.jerzymaj.major.models;

import com.jerzymaj.major.models.enums.ChangeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private ChangeType changeType;

    private String oldValue;

    @Column(nullable = false)
    private String newValue;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timeStamp;

    @Column(nullable = false)
    private String changedBy;
}
