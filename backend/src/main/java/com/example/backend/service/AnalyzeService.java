package com.example.backend.service;

import com.example.backend.entity.Assessment;
import com.example.backend.entity.Enum.AssessmentStatus;
import com.example.backend.entity.Finding;
import com.example.backend.entity.ActionStep;
import com.example.backend.repository.AssessmentRepository;
import com.example.backend.repository.FindingRepository;
import com.example.backend.repository.ActionStepRepository;
import com.example.backend.validation.ScanDataNormalizer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyzeService {

    private final LlmService llmService;
    private final ScanDataNormalizer scanDataNormalizer;
    private final AssessmentRepository assessmentRepository;
    private final FindingRepository findingRepository;
    private final ActionStepRepository actionStepRepository;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            Bạn là một chuyên gia bảo mật và Pentester chuyên nghiệp.
            Nhiệm vụ của bạn là phân tích dữ liệu trinh sát (Reconnaissance) từ các công cụ (NMAP, GOBUSTER, SSLSCAN, WHATWEB, NIKTO...):
            
            1. XÁC ĐỊNH LỖ HỔNG & NGUY CƠ (Findings):
               - Đánh giá các cổng mở bất thường, giao thức cũ (TLSv1.0, TLSv1.1 -> MEDIUM/LOW).
               - Đánh giá các thư mục nhạy cảm tìm thấy như /admin, /bank, /util (-> HIGH/MEDIUM).
               - Đánh giá các cookie thiếu cờ bảo mật hoặc thông tin công nghệ lộ diện.
               - Phân loại mức độ: CRITICAL, HIGH, MEDIUM, LOW, INFO.
            
            2. ĐỀ XUẤT HÀNH ĐỘNG TIẾP THEO (Actions):
               - Đề xuất tối đa 3 công cụ cụ thể (ví dụ: sqlmap, nuclei, hydra, ncrack, nikto, dirsearch, curl).
               - Chỉ rõ mục tiêu (target) và lý do khai thác tương ứng với các Finding ở trên.
            
            ĐỊNH DẠNG JSON BẮT BUỘC:
            {
              "findings": [
                {
                  "title": "Tên phát hiện/lỗ hổng",
                  "severity": "CRITICAL|HIGH|MEDIUM|LOW|INFO",
                  "description": "Mô tả chi tiết và nguy cơ",
                  "tool": "Tên công cụ phát hiện"
                }
              ],
              "actions": [
                {
                  "tool_name": "Tên công cụ đề xuất",
                  "target": "URL hoặc IP:Port mục tiêu",
                  "reason": "Lý do và mục đích tấn công thử nghiệm"
                }
              ]
            }
            
            LƯU Ý: Tuyệt đối chỉ trả về JSON, không kèm giải thích bên ngoài.
            """;

    @Transactional
    public String runAnalysis(Long assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Assessment ID: " + assessmentId));

        log.info("Bắt đầu chuẩn bị dữ liệu phân tích cho Assessment ID: {}", assessmentId);
        String scanData = scanDataNormalizer.normalizeForLlm(assessmentId);
        log.info("Dữ liệu gửi sang AI:\n{}", scanData);

        String userPrompt = "Hãy phân tích dữ liệu rà quét sau và trả về báo cáo JSON:\n\n" + scanData;

        log.info("Gửi dữ liệu tới LLM...");
        String jsonResult = llmService.analyzeScanResults(SYSTEM_PROMPT, userPrompt);
        log.info("Phân tích hoàn tất! Bắt đầu lưu vào Database...");

        // Bóc tách JSON và lưu vào CSDL
        saveAnalysisToDb(assessment, jsonResult);

        // Đổi trạng thái Assessment sang PENDING_APPROVAL (Chờ duyệt kế hoạch tấn công)
        assessment.setStatus(AssessmentStatus.PENDING_APPROVAL);
        assessmentRepository.save(assessment);

        return jsonResult;
    }

    private void saveAnalysisToDb(Assessment assessment, String jsonResult) {
        try {
            JsonNode root = objectMapper.readTree(jsonResult);

            // 1. Lưu Findings
            JsonNode findingsNode = root.path("findings");
            if (findingsNode.isArray()) {
                for (JsonNode f : findingsNode) {
                    Finding finding = Finding.builder()
                            .assessment(assessment)
                            .title(f.path("title").asText("Lỗ hổng không tên"))
                            .severity(f.path("severity").asText("INFO"))
                            .description(f.path("description").asText(""))
                            .tool(f.path("tool").asText("UNKNOWN"))
                            .build();
                    findingRepository.save(finding);
                }
            }

            // 2. Lưu Action Steps
            JsonNode actionsNode = root.path("actions");
            if (actionsNode.isArray()) {
                for (JsonNode a : actionsNode) {
                    ActionStep action = ActionStep.builder()
                            .assessment(assessment)
                            .toolName(a.path("tool_name").asText("unknown"))
                            .target(a.path("target").asText(""))
                            .reason(a.path("reason").asText(""))
                            .status("PROPOSED")
                            .build();
                    actionStepRepository.save(action);
                }
            }

            log.info("Đã lưu Findings và Action Steps vào Database thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi parse JSON để lưu DB: {}", e.getMessage(), e);
        }
    }

    // Các hàm getter để phục vụ Controller
    public List<Finding> getFindings(Long assessmentId) {
        return findingRepository.findByAssessmentId(assessmentId);
    }

    public List<ActionStep> getActionSteps(Long assessmentId) {
        return actionStepRepository.findByAssessmentId(assessmentId);
    }
}