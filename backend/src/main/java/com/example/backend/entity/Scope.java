package com.example.backend.entity;
import com.example.backend.entity.Enum.AssessmentType;
import com.example.backend.entity.Enum.ScopeType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "scopes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scope {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "assessment_id",
            nullable = false
    )
    private Assessment assessment;

    @Column(nullable = false, length = 500)
    private String target;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "target_type",
            nullable = false,
            length = 20
    )
    private ScopeType targetType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
