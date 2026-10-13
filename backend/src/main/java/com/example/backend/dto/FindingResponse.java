package com.example.backend.dto;

import java.time.LocalDateTime;

public record FindingResponse(
        Long id,
        String title,
        String severity,
        String description,
        String tool,
        LocalDateTime createdAt
) {
}
