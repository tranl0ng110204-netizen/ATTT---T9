package com.example.backend.dto.tool;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ToolResult {
    private String toolName;          // e.g. "MASSCAN"
    private String rawOutput;         // stdout (or error)
    private String status;            // SUCCESS, FAILED, TIMEOUT
}
