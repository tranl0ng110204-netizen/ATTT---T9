package com.example.backend.dto;

import com.example.backend.entity.Enum.AssessmentStatus;
import com.example.backend.entity.Enum.AssessmentType;

import java.time.LocalDateTime;

public record AssessmentResponse(
        Long id,
        String name,
        AssessmentType type,
        AssessmentStatus status,
        LocalDateTime createdAt
) {
}
