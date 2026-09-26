package com.example.backend.dto.parse;

public record WafInfo(
        String url,      // "http://demo.testfire.net"
        String wafName,  // "Cloudflare" hoặc "No WAF detected"
        boolean detected
) {
}
