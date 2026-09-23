package com.example.backend.entity;

import com.example.backend.entity.Enum.AssessmentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssessmentType type;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "assessment",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Scope> scopes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
