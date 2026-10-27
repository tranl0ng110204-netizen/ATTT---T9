package com.example.backend.dto.attackpathnode;

import java.time.LocalDateTime;

public record AttackPathNode(
        int stepNumber,
        String phase,           // RECONNAISSANCE, VULNERABILITY_DETECTION, VERIFICATION
        String tool,            // nmap, dirsearch, sqlmap, nuclei...
        String target,          // Target URL hoặc IP
        String description,     // Mục đích hoặc mô tả bước
        String status,          // SUCCESS, FAILED, COMPLETED
        String evidence,        // Bằng chứng quan trọng trích xuất từ log
        LocalDateTime timestamp
) {
}
