package com.example.backend.controller;

import com.example.backend.dto.AssessmentRequest;
import com.example.backend.dto.AssessmentResponse;
import com.example.backend.dto.ScanResultResponse;
import com.example.backend.entity.Assessment;
import com.example.backend.repository.AssessmentRepository;
import com.example.backend.service.AssessmentService;
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
}
