package com.example.backend.util;

import com.example.backend.dto.parse.NiktoFinding;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NiktoParser {
    // Nikto CSV format: "host","IP","port","uri","method","desc"
    private static final Pattern CSV_PATTERN =
            Pattern.compile("\"[^\"]*\",\"[^\"]*\",\"[^\"]*\",\"([^\"]*)\",\"([^\"]*)\",\"([^\"]*)\"");
    // Fallback: dòng bắt đầu bằng "+ "
    private static final Pattern LINE_PATTERN =
            Pattern.compile("^\\+ (\\S+):?\\s+(.*)$");
    public List<NiktoFinding> parse(String rawOutput) {
        List<NiktoFinding> findings = new ArrayList<>();
        if (rawOutput == null || rawOutput.isBlank()) return findings;
        for (String line : rawOutput.split("\\r?\\n")) {
            String trimmed = line.trim();
            // Thử CSV trước
            Matcher csvMatch = CSV_PATTERN.matcher(trimmed);
            if (csvMatch.find()) {
                findings.add(new NiktoFinding(
                        csvMatch.group(1),
                        csvMatch.group(2),
                        csvMatch.group(3)
                ));
                continue;
            }
            // Fallback: parse dòng text
            Matcher lineMatch = LINE_PATTERN.matcher(trimmed);
            if (lineMatch.find()) {
                findings.add(new NiktoFinding(
                        lineMatch.group(1),
                        "GET",
                        lineMatch.group(2).trim()
                ));
            }
        }
        return findings;
    }
}
