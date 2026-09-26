package com.example.backend.dto.parse;

public record SslInfo(
        String status,     // "Accepted" / "Preferred"
        String protocol,   // "TLSv1.2"
        String strength,   // "256 bits"
        String cipher      // "AES256-GCM-SHA384"
) {
}
