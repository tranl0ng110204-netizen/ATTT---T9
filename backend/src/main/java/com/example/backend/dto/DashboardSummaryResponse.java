package com.example.backend.dto;

import java.util.List;
import java.util.Map;
public record DashboardSummaryResponse(
        long totalAssessments,
        long totalFindings,
        Map<String, Long> findingsBySeverity,
        ActionMetrics actionMetrics,
        List<TopFinding> topFindings,
        List<RecentAssessment> recentAssessments
) {
    public record ActionMetrics(
            long totalProposed,
            long totalApproved,
            long totalRejected,
            long totalCompleted,
            double approvalRatePercentage
    ) {}
    public record TopFinding(
            String title,
            String severity,
            long count
    ) {}
    public record RecentAssessment(
            Long id,
            String name,
            String type,
            String status,
            String createdAt
    ) {}
}

