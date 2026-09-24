package com.example.backend.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scan_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Column(nullable = false, length = 50)
    private String toolName;       // "NMAP" hoặc "GOBUSTER"

    @Column(nullable = false, length = 500)
    private String target;

    @Column(columnDefinition = "TEXT")
    private String rawOutput;      // Toàn bộ stdout

    @Column(columnDefinition = "TEXT")
    private String parsedData;     // JSON đã parse (port list hoặc path list)

    @Column(nullable = false, length = 20)
    private String status;         // "RUNNING", "SUCCESS", "FAILED", "TIMEOUT"

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @PrePersist
    public void prePersist() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }
}
