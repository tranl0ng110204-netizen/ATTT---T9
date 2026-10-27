package com.example.backend.controller;


import com.example.backend.dto.ActionStepResponse;
import com.example.backend.dto.ExecutionLogResponse;
import com.example.backend.dto.ExecutionResult;
import com.example.backend.dto.PolicyResult;
import com.example.backend.service.ActionStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/actions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // ket noi frontend
public class ActionStepController {
    private final ActionStepService actionStepService;

    /**
     * Phê duyệt hành động
     * POST /api/actions/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<ActionStepResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(actionStepService.approveAction(id));
    }
    /**
     * Từ chối hành động
     * POST /api/actions/{id}/reject
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<ActionStepResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(actionStepService.rejectAction(id));
    }

    @GetMapping("/{id}/policy-check")
    public ResponseEntity<PolicyResult> checkPolicy(@PathVariable Long id) {
        return ResponseEntity.ok(actionStepService.checkPolicy(id));
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<ExecutionResult> execute(@PathVariable Long id) {
        ExecutionResult result = actionStepService.executeAction(id);
        return ResponseEntity.ok(result);
    }

    /**
     * Lấy toàn bộ Log terminal và kết quả thực thi đã lưu
     * GET /api/actions/{id}/result
     */
    @GetMapping("/{id}/result")
    public ResponseEntity<ExecutionLogResponse> getExecutionResult(@PathVariable Long id) {
        return ResponseEntity.ok(actionStepService.getExecutionResult(id));
    }


}
