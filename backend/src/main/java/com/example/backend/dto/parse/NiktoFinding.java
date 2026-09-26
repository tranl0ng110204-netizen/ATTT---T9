package com.example.backend.dto.parse;

public record NiktoFinding(
        String uri,          // "/admin"
        String method,       // "GET"
        String description   // "Directory indexing found"
) {
}
