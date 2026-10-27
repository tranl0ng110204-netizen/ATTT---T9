package com.example.backend.dto;

public record ExecutionResult(
        int exitCode,
        String output,
        boolean timedOut
) {
}
