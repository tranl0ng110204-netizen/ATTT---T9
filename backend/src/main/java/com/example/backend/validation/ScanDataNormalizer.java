package com.example.backend.validation;

import com.example.backend.entity.ScanResult;
import com.example.backend.repository.ScanResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ScanDataNormalizer {
    private final ScanResultRepository scanResultRepository;
    /**
     * Lấy toàn bộ kết quả quét của 1 Assessment và chuyển thành chuỗi Text tóm tắt
     */
    public String normalizeForLlm(Long assessmentId) {
        List<ScanResult> results = scanResultRepository.findByAssessmentId(assessmentId);
        StringBuilder sb = new StringBuilder();

        sb.append("DỮ LIỆU SCAN ĐẦU VÀO:\n");
        sb.append("======================\n");
        for (ScanResult r : results) {
            // Chỉ lấy những tool quét thành công và có dữ liệu JSON
            if ("SUCCESS".equals(r.getStatus()) && r.getParsedData() != null) {
                sb.append("Tool: ").append(r.getToolName()).append("\n");
                sb.append("Target: ").append(r.getTarget()).append("\n");
                sb.append("Kết quả JSON: ").append(r.getParsedData()).append("\n");
                sb.append("----------------------\n");
            }
        }

        return sb.toString();
    }
}
