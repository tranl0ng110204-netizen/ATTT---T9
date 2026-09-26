package com.example.backend.util;

import com.example.backend.dto.parse.WafInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Wafw00fParser {
    // "The site http://x is behind Cloudflare (Cloudflare Inc.)"
    private static final Pattern DETECTED_PATTERN =
            Pattern.compile("The site (\\S+) is behind (.+?) \\(");
    // "No WAF detected by the generic detection"
    private static final Pattern NOT_DETECTED_PATTERN =
            Pattern.compile("No WAF detected");
    public List<WafInfo> parse(String rawOutput) {
        List<WafInfo> results = new ArrayList<>();
        if (rawOutput == null || rawOutput.isBlank()) return results;
        for (String line : rawOutput.split("\\r?\\n")) {
            Matcher detected = DETECTED_PATTERN.matcher(line);
            if (detected.find()) {
                results.add(new WafInfo(detected.group(1), detected.group(2).trim(), true));
                continue;
            }
            Matcher notDetected = NOT_DETECTED_PATTERN.matcher(line);
            if (notDetected.find()) {
                results.add(new WafInfo("", "No WAF detected", false));
            }
        }
        return results;
    }
}
