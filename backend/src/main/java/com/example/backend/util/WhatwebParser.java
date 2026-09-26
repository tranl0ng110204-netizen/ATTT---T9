package com.example.backend.util;

import com.example.backend.dto.parse.TechInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class WhatwebParser {
    // WhatWeb output: "http://x [200 OK] Apache[2.4.49], Country[US], ..."
    // Mỗi plugin nằm trong dấu [], tách bởi ", "
    private static final Pattern PLUGIN_PATTERN =
            Pattern.compile("([A-Za-z][A-Za-z0-9_-]+)\\[([^\\]]+)\\]");
    public List<TechInfo> parse(String rawOutput) {
        List<TechInfo> results = new ArrayList<>();
        if (rawOutput == null || rawOutput.isBlank()) return results;
        for (String line : rawOutput.split("\\r?\\n")) {
            Matcher m = PLUGIN_PATTERN.matcher(line);
            while (m.find()) {
                String name = m.group(1);
                String version = m.group(2);
                // Bỏ qua các trường meta không phải technology
                if (name.equals("Country") || name.equals("IP")
                        || name.equals("Title") || name.equals("HTTPServer")) {
                    continue;
                }
                results.add(new TechInfo(name, version, categorize(name)));
            }
        }
        return results;
    }
    private String categorize(String name) {
        return switch (name.toLowerCase()) {
            case "apache", "nginx", "iis", "lighttpd" -> "Web Server";
            case "php", "asp", "jsp" -> "Language";
            case "jquery", "bootstrap", "react" -> "JS Framework";
            case "wordpress", "joomla", "drupal" -> "CMS";
            default -> "Technology";
        };
    }
}
