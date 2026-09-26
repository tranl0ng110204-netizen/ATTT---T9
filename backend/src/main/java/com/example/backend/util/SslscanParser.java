package com.example.backend.util;

import com.example.backend.dto.parse.SslInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SslscanParser {
    // "Accepted  TLSv1.2  256 bits  AES256-GCM-SHA384"
    private static final Pattern CIPHER_PATTERN =
            Pattern.compile("^\\s*(Accepted|Preferred)\\s+(\\S+)\\s+(\\d+\\s+bits)\\s+(.+)$");
    public List<SslInfo> parse(String rawOutput) {
        List<SslInfo> results = new ArrayList<>();
        if (rawOutput == null || rawOutput.isBlank()) return results;
        for (String line : rawOutput.split("\\r?\\n")) {
            Matcher m = CIPHER_PATTERN.matcher(line);
            if (m.find()) {
                results.add(new SslInfo(
                        m.group(1),
                        m.group(2),
                        m.group(3),
                        m.group(4).trim()
                ));
            }
        }
        return results;
    }
}
