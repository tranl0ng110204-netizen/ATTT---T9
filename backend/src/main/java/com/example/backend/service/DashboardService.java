package com.example.backend.service;

import com.example.backend.dto.DashboardSummaryResponse;
import com.example.backend.entity.ActionStep;
import com.example.backend.entity.Assessment;
import com.example.backend.entity.Finding;
import com.example.backend.repository.ActionStepRepository;
import com.example.backend.repository.AssessmentRepository;
import com.example.backend.repository.FindingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final AssessmentRepository assessmentRepository;
    private final FindingRepository findingRepository;
    private final ActionStepRepository actionStepRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        // 1. Tổng số assessment
        long totalAssessments = assessmentRepository.count();
        // 2. Thống kê Findings theo mức độ nghiêm trọng
        List<Finding> allFindings = findingRepository.findAll();
        long totalFindings = allFindings.size();
        Map<String, Long> severityCounts = allFindings.stream()
                .collect(Collectors.groupingBy(
                        f -> f.getSeverity() != null ? f.getSeverity().toUpperCase() : "INFO",
                        Collectors.counting()
                ));
        // 3. Thống kê hành động (Human-in-the-loop metrics)
        List<ActionStep> allActions = actionStepRepository.findAll();
        long totalActions = allActions.size();
        long approved = allActions.stream().filter(a -> "APPROVED".equalsIgnoreCase(a.getStatus()) || "COMPLETED".equalsIgnoreCase(a.getStatus())).count();
        long rejected = allActions.stream().filter(a -> "REJECTED".equalsIgnoreCase(a.getStatus())).count();
        long completed = allActions.stream().filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus())).count();
        long proposed = allActions.stream().filter(a -> "PROPOSED".equalsIgnoreCase(a.getStatus())).count();
        double approvalRate = (approved + rejected) > 0
                ? Math.round(((double) approved / (approved + rejected) * 100.0) * 10.0) / 10.0
                : 0.0;
        DashboardSummaryResponse.ActionMetrics actionMetrics = new DashboardSummaryResponse.ActionMetrics(proposed, approved, rejected, completed, approvalRate);
        // 4. Top 5 lỗ hổng xuất hiện nhiều nhất
        Map<String, Long> findingOccurrences = allFindings.stream()
                .collect(Collectors.groupingBy(Finding::getTitle, Collectors.counting()));
        List<DashboardSummaryResponse.TopFinding> topFindings = findingOccurrences.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    String sev = allFindings.stream()
                            .filter(f -> f.getTitle().equals(e.getKey()))
                            .map(Finding::getSeverity)
                            .findFirst().orElse("INFO");
                    return new DashboardSummaryResponse.TopFinding(e.getKey(), sev, e.getValue());
                })
                .toList();
        // 5. Các Assessment gần đây nhất
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        List<DashboardSummaryResponse.RecentAssessment> recent = assessmentRepository.findAll().stream()
                .sorted(Comparator.comparing(Assessment::getId).reversed())
                .limit(5)
                .map(a -> new DashboardSummaryResponse.RecentAssessment(
                        a.getId(),
                        a.getName(),
                        a.getType() != null ? a.getType().name() : "WEB",
                        a.getStatus() != null ? a.getStatus().name() : "NEW",
                        a.getCreatedAt() != null ? a.getCreatedAt().format(dtf) : "N/A"
                ))
                .toList();
        return new DashboardSummaryResponse(
                totalAssessments,
                totalFindings,
                severityCounts,
                actionMetrics,
                topFindings,
                recent
        );
    }
}
