package com.example.backend.service;

import com.example.backend.dto.attackpathnode.AttackPathNode;
import com.example.backend.dto.attackpathnode.AttackPathResponse;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AttackPathService {
    private final AssessmentRepository assessmentRepository;
    private final ScanResultRepository scanResultRepository;
    private final ActionStepRepository actionStepRepository;
    private final FindingRepository findingRepository;
    private final ExecutionLogRepository executionLogRepository;

    @Transactional(readOnly = true)
    public AttackPathResponse buildAttackPath(Long assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Assessment ID: " + assessmentId));
        List<AttackPathNode> nodes = new ArrayList<>();
        int step = 1;

        // 1. Giai đoạn RECONNAISSANCE: Thu thập từ các công cụ trinh sát thành công
        List<ScanResult> scanResults = scanResultRepository.findByAssessmentId(assessmentId);
        for (ScanResult sr : scanResults) {
            if ("SUCCESS".equalsIgnoreCase(sr.getStatus())) {
                String evidenceSummary = extractScanEvidence(sr);
                nodes.add(new AttackPathNode(
                        step++,
                        "RECONNAISSANCE",
                        sr.getToolName(),
                        sr.getTarget(),
                        "Rà quét bề mặt tấn công qua công cụ " + sr.getToolName(),
                        "SUCCESS",
                        evidenceSummary,
                        sr.getFinishedAt() != null ? sr.getFinishedAt() : sr.getStartedAt()
                ));
            }
        }
        // 2. Giai đoạn VULNERABILITY_DETECTION: Lỗ hổng AI phát hiện
        List<Finding> findings = findingRepository.findByAssessmentId(assessmentId);
        for (Finding f : findings) {
            nodes.add(new AttackPathNode(
                    step++,
                    "VULNERABILITY_DETECTION",
                    f.getTool(),
                    assessment.getName(),
                    "[" + f.getSeverity() + "] " + f.getTitle(),
                    "IDENTIFIED",
                    f.getDescription(),
                    f.getCreatedAt()
            ));
        }
        // 3. Giai đoạn VERIFICATION: Các hành động đã thực thi và bằng chứng
        List<ActionStep> actions = actionStepRepository.findByAssessmentId(assessmentId);
        for (ActionStep a : actions) {
            if ("COMPLETED".equalsIgnoreCase(a.getStatus()) || "FAILED".equalsIgnoreCase(a.getStatus())) {
                // Lấy log thực thi
                ExecutionLog logEntity = executionLogRepository.findTopByActionStepIdOrderByIdDesc(a.getId()).orElse(null);
                String evidence = "Không có bằng chứng cụ thể";
                LocalDateTime executedTime = a.getCreatedAt();
                if (logEntity != null) {
                    evidence = extractExecutionEvidence(a.getToolName(), logEntity.getRawOutput());
                    executedTime = logEntity.getFinishedAt();
                }
                nodes.add(new AttackPathNode(
                        step++,
                        "VERIFICATION",
                        a.getToolName(),
                        a.getTarget(),
                        "Kiểm thử: " + a.getReason(),
                        a.getStatus(),
                        evidence,
                        executedTime
                ));
            }
        }
        return new AttackPathResponse(
                assessment.getId(),
                assessment.getName(),
                nodes.size(),
                nodes
        );
    }
    /**
     * Bóc tách bằng chứng từ kết quả quét trinh sát
     */
    private String extractScanEvidence(ScanResult sr) {
        if (sr.getParsedData() != null && !sr.getParsedData().isBlank()) {
            String data = sr.getParsedData();
            if (data.length() > 200) {
                return data.substring(0, 200) + "...";
            }
            return data;
        }
        return "Hoàn thành quét với mã trạng thái " + sr.getStatus();
    }
    /**
     * Bóc tách dòng then chốt (Evidence) từ terminal output của Sqlmap, Nuclei, Dirsearch...
     */
    private String extractExecutionEvidence(String toolName, String rawOutput) {
        if (rawOutput == null || rawOutput.isBlank()) {
            return "Không có output";
        }
        String tool = toolName.toLowerCase();
        String[] lines = rawOutput.split("\n");
        // Nếu là sqlmap: Tìm dòng thông báo phát hiện injection
        if (tool.contains("sqlmap")) {
            for (String line : lines) {
                if (line.contains("is vulnerable") || line.contains("sqlmap identified the following injection point") || line.contains("back-end DBMS")) {
                    return line.trim();
                }
            }
        }
        // Nếu là nuclei: Tìm dòng lỗ hổng có tag severity
        if (tool.contains("nuclei")) {
            for (String line : lines) {
                if (line.contains("[critical]") || line.contains("[high]") || line.contains("[medium]") || line.contains("[low]")) {
                    return line.trim();
                }
            }
        }
        // Nếu là dirsearch: Tìm các dòng có mã HTTP 200
        if (tool.contains("dirsearch")) {
            for (String line : lines) {
                if (line.contains("200 -")) {
                    return "Tìm thấy đường dẫn phản hồi 200: " + line.trim();
                }
            }
        }
        // Mặc định: Trích xuất dòng kết luận hoặc 150 ký tự đầu tiên
        for (int i = lines.length - 1; i >= 0; i--) {
            if (!lines[i].isBlank() && !lines[i].startsWith("===")) {
                return lines[i].trim();
            }
        }
        return rawOutput.length() > 150 ? rawOutput.substring(0, 150) + "..." : rawOutput;
    }
}
