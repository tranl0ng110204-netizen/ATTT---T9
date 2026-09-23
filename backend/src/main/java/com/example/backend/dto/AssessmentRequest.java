package com.example.backend.dto;

import com.example.backend.entity.Enum.AssessmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AssessmentRequest(
        @NotBlank(message = "Name không được để trống")
        @Size(
                max = 255,
                message = "Name không được vượt quá 255 ký tự"
        )
        String name,

        @NotNull(message = "Type không được để trống")
        AssessmentType type,
        List<String> targets
) {
}
