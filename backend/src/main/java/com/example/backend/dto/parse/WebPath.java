package com.example.backend.dto.parse;

public record WebPath(
        String path,    // "/admin"
        int statusCode  // 200, 301, 302
) {
}
