package com.example.backend.util;

import com.example.backend.dto.parse.WebPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class GobusterParser {
    // Ví dụ dòng Gobuster: "images               (Status: 301) [Size: 1234]"
    // Bỏ yêu cầu bắt buộc dấu / ở đầu
    private static final Pattern LINE_PATTERN =
            Pattern.compile("^(\\S+)\\s+\\(Status:\\s*(\\d{3})\\)");
    public List<WebPath> parse(String rawOutput) {
        List<WebPath> paths = new ArrayList<>();
        if (rawOutput == null || rawOutput.isBlank()) return paths;
        for (String line : rawOutput.split("\\n")) {
            Matcher m = LINE_PATTERN.matcher(line.trim());
            if (m.find()) {
                String path = m.group(1);
                int status = Integer.parseInt(m.group(2));
                paths.add(new WebPath(path, status));
            }
        }
        return paths;
    }
}
