package com.jerzymaj.major.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "labels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    @ToString.Include
    private String name;

    @Column(nullable = false)
    private String color;
}
