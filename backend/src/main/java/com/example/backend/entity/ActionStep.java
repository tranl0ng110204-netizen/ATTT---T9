package com.example.backend.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "action_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id")
    private Assessment assessment;
    @Column(name = "tool_name")
    private String toolName;
    private String target;
    private String reason;
    private String status;
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
