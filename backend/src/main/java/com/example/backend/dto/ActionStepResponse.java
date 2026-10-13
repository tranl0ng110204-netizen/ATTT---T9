package com.example.backend.dto;

import java.time.LocalDateTime;

public record ActionStepResponse(
        Long id,
        String toolName,
        String target,
        String reason,
        String status,
        LocalDateTime createdAt
) {
}
