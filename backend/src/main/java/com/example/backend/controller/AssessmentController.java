package com.example.backend.controller;

import com.example.backend.dto.AssessmentRequest;
import com.example.backend.dto.AssessmentResponse;
import com.example.backend.service.AssessmentService;
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
}
