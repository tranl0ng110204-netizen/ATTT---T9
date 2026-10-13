package com.example.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LlmService {
    @Value("${gemini.api.key}")
    private String apiKey;
    @Value("${gemini.api.url}")
    private String apiUrlTemplate;
    @Value("${gemini.api.model}")
    private String model;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    public LlmService(ObjectMapper objectMapper) {
        // Tự khởi tạo trực tiếp bằng RestClient.create()
        this.restClient = RestClient.create();
        this.objectMapper = objectMapper;
    }
    /**
     * Gửi Prompt tới OpenAI và nhận về kết quả (ép trả về dạng JSON_OBJECT)
     */
    public String analyzeScanResults(String systemPrompt, String userPrompt) {
        log.info("Đang gửi request phân tích tới Google Gemini (Model: {})...", model);
        // Tạo Body Request
        Map<String, Object> requestBody = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", systemPrompt))
                ),
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", userPrompt)))
                ),
                "generationConfig", Map.of(
                        "response_mime_type", "application/json"
                )
        );
        try {
            // Dùng Map để Spring tự động điền giá trị vào {model} và {key} trong URL
            Map<String, String> uriVariables = Map.of(
                    "model", model.trim(),
                    "key", apiKey.trim()
            );
            // Gọi API
            String responseStr = restClient.post()
                    .uri(apiUrlTemplate, uriVariables) // <-- Sửa dòng này
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
            JsonNode rootNode = objectMapper.readTree(responseStr);
            String aiContent = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
            log.info("Đã nhận kết quả phân tích từ Gemini thành công!");
            return aiContent;
        } catch (Exception e) {
            log.error("Lỗi khi gọi Gemini API: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể phân tích dữ liệu bằng AI lúc này.");
        }
    }
}
