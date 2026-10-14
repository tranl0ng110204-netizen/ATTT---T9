package com.example.backend.dto;
import java.util.List;

public record PolicyResult(
        boolean passed,
        List<String> violations) {
    public static PolicyResult success() {
        return new PolicyResult(true, List.of());
    }
    public static PolicyResult fail(List<String> violations) {
        return new PolicyResult(false, violations);
    }
}
