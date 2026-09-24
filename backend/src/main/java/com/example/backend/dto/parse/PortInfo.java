package com.example.backend.dto.parse;

public record PortInfo(
        int port,
        String protocol,  // "tcp" / "udp"
        String state,     // "open" / "filtered"
        String service    // "http" / "ssh" / "unknown"
) {
}
