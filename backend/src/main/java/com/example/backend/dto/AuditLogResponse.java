package com.example.backend.dto;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        String actionType,
        String actor,
        String target,
        String status,
        String details,
        LocalDateTime createdAt
) {
}
