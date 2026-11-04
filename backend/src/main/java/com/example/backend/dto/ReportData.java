package com.example.backend.dto;

import com.example.backend.dto.attackpathnode.AttackPathNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
public record ReportData(
        Long assessmentId,
        String assessmentName,
        String target,
        String assessmentType,
        LocalDateTime generatedAt,
        RiskSummary riskSummary,
        List<FindingResponse> findings,
        List<AttackPathNode> attackPath,
        List<String> executedTools


) {
    public record RiskSummary(
            int totalFindings,
            int criticalCount,
            int highCount,
            int mediumCount,
            int lowCount,
            int infoCount,
            String overallRiskLevel
    ) {}
}
