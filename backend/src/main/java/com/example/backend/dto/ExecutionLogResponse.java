package com.example.backend.dto;

import java.time.LocalDateTime;

public record ExecutionLogResponse(
        Long id,
        Long actionStepId,
        int exitCode,
        String rawOutput,
        boolean timedOut,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
}
