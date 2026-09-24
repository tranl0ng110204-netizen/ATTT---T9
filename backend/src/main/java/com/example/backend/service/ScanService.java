package com.example.backend.service;
import com.example.backend.dto.ScanResultResponse;
import com.example.backend.entity.Assessment;
import com.example.backend.entity.Enum.AssessmentStatus;
import com.example.backend.entity.Enum.AssessmentType;
import com.example.backend.entity.ScanResult;
import com.example.backend.repository.AssessmentRepository;
import com.example.backend.repository.ScanResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class ScanService {
    private final AssessmentRepository assessmentRepository;
    private final ScanResultRepository scanResultRepository;
    /**
     * Khởi động quét bất đồng bộ cho một Assessment
     */
    @Transactional
    public void startScan(Long assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy Assessment ID: " + assessmentId));
        if (assessment.getScopes().isEmpty()) {
            throw new IllegalArgumentException("Assessment chưa có mục tiêu (scope)");
        }
        // Cập nhật trạng thái
        assessment.setStatus(AssessmentStatus.SCANNING);
        assessmentRepository.save(assessment);
        String target = assessment.getScopes().get(0).getTarget();
        String toolName = (assessment.getType() == AssessmentType.WEB) ? "GOBUSTER" : "NMAP";
        // Tạo record theo dõi
        ScanResult scanResult = ScanResult.builder()
                .assessment(assessment)
                .toolName(toolName)
                .target(target)
                .status("RUNNING")
                .build();
        scanResultRepository.save(scanResult);
        // Chạy quét ngầm
        executeScanAsync(scanResult.getId(), assessment.getId(), toolName, target);
    }
    /**
     * Chạy script wrapper trong Kali Worker (Async)
     */
    @Async
    public void executeScanAsync(Long scanResultId, Long assessmentId,
                                 String toolName, String target) {
        try {
            List<String> command = buildCommand(toolName, target);
            log.info("Bắt đầu quét [{}] mục tiêu: {}", toolName, target);
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true); // Gộp lỗi và kết quả làm một

            java.io.File tempFile = java.io.File.createTempFile("scan_result_", ".tmp");
            pb.redirectOutput(tempFile);


            Process process = pb.start();

            boolean finished = process.waitFor(300, TimeUnit.SECONDS);
            String output;
            if (!finished) {
                process.destroyForcibly();
                tempFile.delete();
                updateScanResult(scanResultId, assessmentId, null, "TIMEOUT");
                return;
            }
            output = java.nio.file.Files.readString(tempFile.toPath());
            tempFile.delete(); // Đọc xong thì xóa file tạm đi

            if (process.exitValue() != 0) {
                updateScanResult(scanResultId, assessmentId, output, "FAILED");
                return;
            }
            updateScanResult(scanResultId, assessmentId, output, "SUCCESS");
        } catch (Exception e) {
            log.error("Lỗi khi quét: {}", e.getMessage(), e);
            updateScanResult(scanResultId, assessmentId,
                    "ERROR: " + e.getMessage(), "FAILED");
        }
    }
    /**
     * Xây dựng câu lệnh tùy thuộc vào loại tool
     */
    private List<String> buildCommand(String toolName, String target) {
        return switch (toolName) {
            // Gọi bash script wrapper — script đã được tối ưu cờ, không dùng -sV
            case "NMAP" -> List.of(
                    "docker", "exec", "kali-worker",
                    "bash", "/scripts/run_nmap.sh", target
            );
            case "GOBUSTER" -> List.of(
                    "docker", "exec", "kali-worker",
                    "bash", "/scripts/run_gobuster.sh", target
            );
            default -> throw new IllegalArgumentException("Tool không được hỗ trợ: " + toolName);
        };
    }
    /**
     * Cập nhật kết quả quét vào DB
     */
    @Transactional
    public void updateScanResult(Long scanResultId, Long assessmentId,
                                 String output, String status) {
        ScanResult result = scanResultRepository.findById(scanResultId).orElse(null);
        if (result != null) {
            result.setRawOutput(output);
            result.setStatus(status);
            result.setFinishedAt(LocalDateTime.now());
            scanResultRepository.save(result);
        }
        // Cập nhật trạng thái Assessment
        Assessment assessment = assessmentRepository.findById(assessmentId).orElse(null);
        if (assessment != null) {
            assessment.setStatus(AssessmentStatus.SCAN_COMPLETED);
            assessmentRepository.save(assessment);
        }
        log.info("Quét xong [{}] - Trạng thái: {}", scanResultId, status);
    }
    /**
     * Lấy danh sách kết quả quét của một Assessment
     */
    @Transactional(readOnly = true)
    public List<ScanResultResponse> getResults(Long assessmentId) {
        return scanResultRepository.findByAssessmentId(assessmentId)
                .stream()
                .map(r -> new ScanResultResponse(
                        r.getId(),
                        r.getToolName(),
                        r.getTarget(),
                        r.getRawOutput(),
                        r.getParsedData(),
                        r.getStatus(),
                        r.getStartedAt(),
                        r.getFinishedAt()
                ))
                .toList();
    }
}
