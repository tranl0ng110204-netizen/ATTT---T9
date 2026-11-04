package com.example.backend.controller;

import com.example.backend.dto.ReportData;
import com.example.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assessments/{id}/report")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {
    private final ReportService reportService;
    /**
     * Lấy dữ liệu thô dạng JSON của Báo cáo (Dành cho Frontend dựng Dashboard)
     * GET /api/assessments/{id}/report/data
     */
    @GetMapping("/data")
    public ResponseEntity<ReportData> getReportData(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.compileReportData(id));
    }
    /**
     * Xem trực tiếp Trang Báo cáo HTML định dạng chuyên nghiệp trên trình duyệt
     * GET /api/assessments/{id}/report/html
     */
    @GetMapping(value = "/html", produces = MediaType.TEXT_HTML_VALUE )
    public ResponseEntity<String> getHtmlReport(@PathVariable Long id) {
        String htmlContent = reportService.generateHtmlReport(id);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/html;charset=UTF-8"))
                .body(htmlContent);
    }

    /**
     * Tải trực tiếp file PDF Báo cáo Kiểm thử An toàn về máy
     * GET /api/assessments/{id}/report/pdf
     */
    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPdfReport(@PathVariable Long id) {
        byte[] pdfBytes = reportService.generatePdfReport(id);
        String fileName = "Pentest_Report_Assessment_" + id + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }


}
