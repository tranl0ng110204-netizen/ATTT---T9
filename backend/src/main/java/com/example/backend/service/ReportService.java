package com.example.backend.service;

import com.example.backend.dto.FindingResponse;
import com.example.backend.dto.ReportData;
import com.example.backend.dto.attackpathnode.AttackPathNode;
import com.example.backend.dto.attackpathnode.AttackPathResponse;
import com.example.backend.entity.Assessment;
import com.example.backend.entity.Finding;
import com.example.backend.entity.ScanResult;
import com.example.backend.repository.AssessmentRepository;
import com.example.backend.repository.FindingRepository;
import com.example.backend.repository.ScanResultRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportService {
    private final AssessmentRepository assessmentRepository;
    private final FindingRepository findingRepository;
    private final AttackPathService attackPathService;
    private final ScanResultRepository scanResultRepository;

    /**
     * Tổng hợp toàn bộ dữ liệu kiểm thử thành ReportData
     */
    @Transactional(readOnly = true)
    public ReportData compileReportData(Long assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Assessment ID: " + assessmentId));
        // 1. Lấy danh sách lỗ hổng
        List<Finding> rawFindings = findingRepository.findByAssessmentId(assessmentId);
        List<FindingResponse> findings = rawFindings.stream()
                .map(f -> new FindingResponse(f.getId(), f.getTitle(), f.getSeverity(), f.getDescription(), f.getTool(), f.getCreatedAt()))
                .toList();
        // 2. Thống kê mức độ rủi ro
        int critical = 0, high = 0, medium = 0, low = 0, info = 0;
        for (Finding f : rawFindings) {
            String sev = f.getSeverity() != null ? f.getSeverity().toUpperCase() : "INFO";
            switch (sev) {
                case "CRITICAL" -> critical++;
                case "HIGH" -> high++;
                case "MEDIUM" -> medium++;
                case "LOW" -> low++;
                default -> info++;
            }
        }
        String overallRisk = "LOW";
        if (critical > 0) overallRisk = "CRITICAL";
        else if (high > 0) overallRisk = "HIGH";
        else if (medium > 0) overallRisk = "MEDIUM";
        ReportData.RiskSummary riskSummary = new ReportData.RiskSummary(findings.size(), critical, high, medium, low, info, overallRisk);
        // 3. Lấy Attack Path
        AttackPathResponse attackPathResponse = attackPathService.buildAttackPath(assessmentId);
        // 4. Lấy danh sách công cụ đã chạy
        List<String> executedTools = scanResultRepository.findByAssessmentId(assessmentId).stream()
                .map(ScanResult::getToolName)
                .distinct()
                .toList();
        String target = assessment.getScopes() != null && !assessment.getScopes().isEmpty()
                ? assessment.getScopes().get(0).getTarget()
                : assessment.getName();
        return new ReportData(
                assessment.getId(),
                assessment.getName(),
                target,
                assessment.getType() != null ? assessment.getType().name() : "WEB",
                LocalDateTime.now(),
                riskSummary,
                findings,
                attackPathResponse.nodes(),
                executedTools
        );
    }
    /**
     * Sinh mã HTML chuẩn XHTML cho cả hiển thị Web và in ấn PDF
     */
    @Transactional(readOnly = true)
    public String generateHtmlReport(Long assessmentId) {
        ReportData report = compileReportData(assessmentId);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

        StringBuilder html = new StringBuilder();

        // Chuẩn XHTML 1.0 Strict tương thích 100% với PDF engine
        html.append("""
                <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
                <html xmlns="http://www.w3.org/1999/xhtml" lang="vi">
                <head>
                    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                    <title>Báo Cáo Kiểm Thử An Toàn</title>
                    <style type="text/css">
                        @page { size: A4; margin: 15mm; }
                        body { font-family: 'Arial', sans-serif; line-height: 1.5; color: #333; margin: 0; padding: 0; font-size: 13px; }
                        .container { width: 100%; margin: 0 auto; background: #fff; }
                        .header { border-bottom: 2px solid #2563eb; padding-bottom: 15px; margin-bottom: 20px; }
                        .header h1 { margin: 0; color: #1e293b; font-size: 22px; }
                        .badge { background: #2563eb; color: #fff; padding: 4px 10px; border-radius: 4px; font-weight: bold; font-size: 12px; float: right; margin-top: -30px; }
                        .meta-box { background: #f1f5f9; padding: 10px 15px; border-radius: 4px; margin-bottom: 20px; font-size: 12px; }
                        .meta-box p { margin: 4px 0; }
                        .section-title { font-size: 16px; color: #0f172a; border-left: 4px solid #2563eb; padding-left: 8px; margin-top: 25px; margin-bottom: 12px; font-weight: bold; }
                        
                        /* Thay Grid bằng Table để tương thích PDF 100% */
                        .summary-table { width: 100%; border-collapse: separate; border-spacing: 6px; margin-bottom: 20px; }
                        .summary-cell { padding: 10px; text-align: center; color: #fff; font-weight: bold; border-radius: 4px; }
                        .summary-cell .num { font-size: 20px; display: block; }
                        .bg-critical { background-color: #dc2626; color: #fff; }
                        .bg-high { background-color: #ea580c; color: #fff; }
                        .bg-medium { background-color: #d97706; color: #fff; }
                        .bg-low { background-color: #2563eb; color: #fff; }
                        .bg-info { background-color: #64748b; color: #fff; }
                        
                        /* Tables */
                        table.data-table { width: 100%; border-collapse: collapse; margin-top: 8px; margin-bottom: 20px; }
                        table.data-table th, table.data-table td { border: 1px solid #cbd5e1; padding: 8px 10px; text-align: left; font-size: 12px; }
                        table.data-table th { background-color: #f8fafc; color: #475569; }
                        
                        /* Badges */
                        .tag { display: inline-block; padding: 2px 6px; border-radius: 3px; font-size: 10px; font-weight: bold; color: #fff; }
                        
                        /* Timeline */
                        .timeline-step { margin-bottom: 15px; padding-left: 10px; border-left: 3px solid #cbd5e1; }
                        .step-title { font-weight: bold; color: #1e293b; font-size: 13px; }
                        .step-desc { font-size: 12px; color: #475569; margin: 2px 0; }
                        .evidence { background-color: #f1f5f9; color: #0f172a; border: 1px solid #cbd5e1; font-family: monospace; padding: 6px 10px; border-radius: 3px; font-size: 11px; margin-top: 4px; word-break: break-all; }
                        
                        .footer { margin-top: 30px; border-top: 1px solid #cbd5e1; padding-top: 10px; text-align: center; font-size: 11px; color: #94a3b8; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>BÁO CÁO KIỂM THỬ XÂM NHẬP (PENTEST)</h1>
                            <p style="margin: 4px 0 0 0; color: #64748b; font-size: 12px;">Hệ thống kiểm thử tự động tích hợp Trí tuệ nhân tạo (LLM)</p>
                """);

        html.append("<div class='badge'>").append(report.riskSummary().overallRiskLevel()).append(" RISK</div></div>");

        html.append("<div class='meta-box'>")
                .append("<p><strong>Mục tiêu:</strong> ").append(report.target()).append("</p>")
                .append("<p><strong>Tên phiên:</strong> ").append(report.assessmentName()).append("</p>")
                .append("<p><strong>Thời gian tạo:</strong> ").append(report.generatedAt().format(dtf)).append("</p>")
                .append("<p><strong>Công cụ đã quét:</strong> ").append(String.join(", ", report.executedTools())).append("</p>")
                .append("</div>");

        // Bảng tóm tắt rủi ro (Dùng table thay vì CSS Grid)
        html.append("<div class='section-title'>1. Tóm Tắt Mức Độ Rủi Ro (Executive Summary)</div>")
                .append("<table class='summary-table'><tr>")
                .append("<td class='summary-cell bg-critical'><span class='num'>").append(report.riskSummary().criticalCount()).append("</span>CRITICAL</td>")
                .append("<td class='summary-cell bg-high'><span class='num'>").append(report.riskSummary().highCount()).append("</span>HIGH</td>")
                .append("<td class='summary-cell bg-medium'><span class='num'>").append(report.riskSummary().mediumCount()).append("</span>MEDIUM</td>")
                .append("<td class='summary-cell bg-low'><span class='num'>").append(report.riskSummary().lowCount()).append("</span>LOW</td>")
                .append("<td class='summary-cell bg-info'><span class='num'>").append(report.riskSummary().infoCount()).append("</span>INFO</td>")
                .append("</tr></table>");

        // Chuỗi tấn công
        html.append("<div class='section-title'>2. Chuỗi Tấn Công Thực Nghiệm (Attack Path)</div>");
        for (AttackPathNode node : report.attackPath()) {
            html.append("<div class='timeline-step'>")
                    .append("<div class='step-title'>Bước ").append(node.stepNumber()).append(": [").append(node.phase()).append("] ").append(node.description()).append(" (Tool: ").append(node.tool()).append(")</div>")
                    .append("<div class='step-desc'>").append(node.target()).append("</div>")
                    .append("<div class='evidence'>Bằng chứng: ").append(node.evidence() != null ? node.evidence() : "Không có").append("</div>")
                    .append("</div>");
        }

        // Danh sách lỗ hổng
        html.append("<div class='section-title'>3. Danh Sách Chi Tiết Các Lỗ Hổng (Findings)</div>")
                .append("<table class='data-table'><thead><tr><th style='width: 15%;'>Mức độ</th><th style='width: 30%;'>Tên lỗ hổng</th><th style='width: 15%;'>Công cụ</th><th style='width: 40%;'>Mô tả</th></tr></thead><tbody>");

        if (report.findings().isEmpty()) {
            html.append("<tr><td colspan='4' style='text-align:center;'>Không phát hiện lỗ hổng nguy hiểm nào.</td></tr>");
        } else {
            for (FindingResponse f : report.findings()) {
                String badgeClass = switch (f.severity() != null ? f.severity().toUpperCase() : "INFO") {
                    case "CRITICAL" -> "bg-critical";
                    case "HIGH" -> "bg-high";
                    case "MEDIUM" -> "bg-medium";
                    case "LOW" -> "bg-low";
                    default -> "bg-info";
                };
                html.append("<tr>")
                        .append("<td><span class='tag ").append(badgeClass).append("'>").append(f.severity()).append("</span></td>")
                        .append("<td><strong>").append(f.title()).append("</strong></td>")
                        .append("<td>").append(f.tool()).append("</td>")
                        .append("<td>").append(f.description()).append("</td>")
                        .append("</tr>");
            }
        }

        html.append("""
                            </tbody>
                        </table>
                        <div class="footer">
                            Báo cáo được khởi tạo tự động bởi Hệ thống Thử nghiệm An toàn ATTT-T9.
                        </div>
                    </div>
                </body>
                </html>
                """);

        return html.toString();
    }

    /**
     * Xuất PDF an toàn
     */
    @Transactional(readOnly = true)
    public byte[] generatePdfReport(Long assessmentId) {
        log.info("Bắt đầu kết xuất PDF cho Assessment ID: {}", assessmentId);
        String htmlContent = generateHtmlReport(assessmentId);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            // Nạp font Arial từ Windows nếu có
            File arialFont = new File("C:/Windows/Fonts/arial.ttf");
            if (arialFont.exists()) {
                builder.useFont(arialFont, "Arial");
            }

            builder.withHtmlContent(htmlContent, "");
            builder.toStream(outputStream);
            builder.run();

            log.info("Đã tạo file PDF Báo cáo thành công cho Assessment ID: {}", assessmentId);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Lỗi khi kết xuất file PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo file PDF báo cáo: " + e.getMessage());
        }
    }

}

