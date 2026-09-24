package com.example.backend.controller;

import com.example.backend.dto.AssessmentRequest;
import com.example.backend.dto.AssessmentResponse;
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

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final AssessmentRepository assessmentRepository;
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

    @PostMapping("/{id}/scan")
    public ResponseEntity<String> startScan(@PathVariable Long id){
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Assessment not found"));
        if(assessment.getScopes().isEmpty()){
            throw new RuntimeException("Assessment has no target");
        }
        String target = assessment.getScopes().get(0).getTarget();
        String result = scanService.scan(target);

        return ResponseEntity.ok(result);
    }
}
