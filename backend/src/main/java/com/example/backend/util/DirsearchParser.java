package com.example.backend.util;
import com.example.backend.dto.parse.WebPath;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DirsearchParser {
    // Dirsearch plain format: "200   234B   http://target/admin"
    // hoặc: "/admin   [Status: 200, Size: 234]"
    private static final Pattern PLAIN_PATTERN =
            Pattern.compile("^(\\d{3})\\s+\\S+\\s+(\\S+)$");
    private static final Pattern ALT_PATTERN =
            Pattern.compile("^(\\S+)\\s+\\[Status:\\s*(\\d{3})");
    public List<WebPath> parse(String rawOutput) {
        List<WebPath> paths = new ArrayList<>();
        if (rawOutput == null || rawOutput.isBlank()) return paths;
        for (String line : rawOutput.split("\\r?\\n")) {
            String trimmed = line.trim();
            Matcher plain = PLAIN_PATTERN.matcher(trimmed);
            if (plain.find()) {
                int status = Integer.parseInt(plain.group(1));
                String url = plain.group(2);
                // Trích path từ URL đầy đủ
                String path = url.replaceAll("https?://[^/]+", "");
                if (path.isEmpty()) path = "/";
                paths.add(new WebPath(path, status));
                continue;
            }
            Matcher alt = ALT_PATTERN.matcher(trimmed);
            if (alt.find()) {
                paths.add(new WebPath(alt.group(1), Integer.parseInt(alt.group(2))));
            }
        }
        return paths;
    }
}
