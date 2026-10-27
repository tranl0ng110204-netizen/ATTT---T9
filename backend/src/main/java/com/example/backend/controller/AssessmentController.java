package com.example.backend.controller;

import com.example.backend.dto.AssessmentRequest;
import com.example.backend.dto.AssessmentResponse;
import com.example.backend.dto.ScanResultResponse;
import com.example.backend.dto.ActionStepResponse;
import com.example.backend.dto.FindingResponse;
import com.example.backend.dto.attackpathnode.AttackPathResponse;
import com.example.backend.service.AnalyzeService;
import com.example.backend.service.AssessmentService;
import com.example.backend.service.AttackPathService;
import com.example.backend.service.ScanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final ScanService scanService;
    private final AnalyzeService analyzerService;
    private final AttackPathService attackPathService;

    /**
     * Tạo phiên kiểm thử mới
     * POST /api/assessments
     */
    @PostMapping
    public ResponseEntity<AssessmentResponse> create(
            @Valid @RequestBody AssessmentRequest request
    ) {
        AssessmentResponse response = assessmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lấy danh sách tất cả phiên kiểm thử
     * GET /api/assessments
     */
    @GetMapping
    public ResponseEntity<List<AssessmentResponse>> getAll() {
        return ResponseEntity.ok(assessmentService.getAll());
    }

    /**
     * Lấy chi tiết một phiên kiểm thử theo ID
     * GET /api/assessments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AssessmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.getById(id));
    }

    /**
     * Khởi động quét trinh sát cho một Assessment
     * POST /api/assessments/{id}/scan
     */
    @PostMapping("/{id}/scan")
    public ResponseEntity<Map<String, String>> startScan(@PathVariable Long id) {
        scanService.startScan(id);
        return ResponseEntity.accepted()
                .body(Map.of("message", "Đã bắt đầu quét. Kiểm tra kết quả qua GET /api/assessments/" + id + "/scan-results"));
    }
    /**
     * Xem kết quả quét của một Assessment
     * GET /api/assessments/{id}/scan-results
     */
    @GetMapping("/{id}/scan-results")
    public ResponseEntity<List<ScanResultResponse>> getScanResults(@PathVariable Long id) {
        return ResponseEntity.ok(scanService.getResults(id));
    }

    /**
     * Xem kết quả quét đã được parse (dạng JSON có cấu trúc)
     * GET /api/assessments/{id}/scan-results/parsed
     */
    @GetMapping("/{id}/scan-results/parsed")
    public ResponseEntity<?> getParsedResults(@PathVariable Long id) {
        List<ScanResultResponse> results = scanService.getResults(id);
        // Chỉ lấy các kết quả SUCCESS có parsedData
        var parsed = results.stream()
                .filter(r -> "SUCCESS".equals(r.status()) && r.parsedData() != null)
                .map(r -> Map.of(
                        "id", r.id(),
                        "tool", r.toolName(),
                        "target", r.target(),
                        "startedAt", r.startedAt(),
                        "finishedAt", r.finishedAt(),
                        "result", r.parsedData()   // JSON string đã parse sẵn
                ))
                .toList();
        return ResponseEntity.ok(parsed);
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<?> analyzeWithAi(@PathVariable Long id) {
        // Trả về JSON string thuần mà AI sinh ra
        String aiJson = analyzerService.runAnalysis(id);

        // Vì aiJson đã là chuỗi JSON chuẩn, ta parse nó ra kiểu Map để ResponseEntity serialize đẹp đẽ
        try {
            // Thay com.fasterxml bằng tools (Jackson 3)
            tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
            Object jsonNode = mapper.readValue(aiJson, Object.class);
            return ResponseEntity.ok(jsonNode);
        } catch (Exception e) {
            // Nếu AI vô tình sinh lỗi không phải JSON, trả về raw text
            return ResponseEntity.ok(Map.of("raw_ai_response", aiJson));
        }
    }

    /**
     * Lấy danh sách các lỗ hổng đã được AI phân tích (DTO sạch)
     * GET /api/assessments/{id}/findings
     */
    @GetMapping("/{id}/findings")
    public ResponseEntity<List<FindingResponse>> getFindings(@PathVariable Long id) {
        List<FindingResponse> findings = analyzerService.getFindings(id).stream()
                .map(f -> new FindingResponse(
                        f.getId(),
                        f.getTitle(),
                        f.getSeverity(),
                        f.getDescription(),
                        f.getTool(),
                        f.getCreatedAt()
                ))
                .toList();
        return ResponseEntity.ok(findings);
    }
    /**
     * Lấy danh sách các bước hành động/kế hoạch tấn công mà AI đề xuất (DTO sạch)
     * GET /api/assessments/{id}/plans
     */
    @GetMapping("/{id}/plans")
    public ResponseEntity<List<ActionStepResponse>> getActionPlans(@PathVariable Long id) {
        List<ActionStepResponse> plans = analyzerService.getActionSteps(id).stream()
                .map(a -> new ActionStepResponse(
                        a.getId(),
                        a.getToolName(),
                        a.getTarget(),
                        a.getReason(),
                        a.getStatus(),
                        a.getCreatedAt()
                ))
                .toList();
        return ResponseEntity.ok(plans);
    }

    /**
     * Lấy biểu đồ chuỗi luồng tấn công (Attack Path) hoàn chỉnh
     * GET /api/assessments/{id}/attack-path
     */
    @GetMapping("/{id}/attack-path")
    public ResponseEntity<AttackPathResponse> getAttackPath(@PathVariable Long id) {
        return ResponseEntity.ok(attackPathService.buildAttackPath(id));
    }

}
