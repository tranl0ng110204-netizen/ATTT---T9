package com.example.backend.dto;

import java.time.LocalDateTime;

public record ScanResultResponse(
        Long id,
        String toolName,
        String target,
        String rawOutput,
        String parsedData,
        String status,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
}
