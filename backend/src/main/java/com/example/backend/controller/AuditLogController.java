package com.example.backend.controller;

import com.example.backend.dto.AuditLogResponse;
import com.example.backend.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuditLogController {
    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getLogs(@RequestParam(required = false, name = "type") String type) {
        return ResponseEntity.ok(auditLogService.getLogs(type));
    }
}
