package com.example.backend.service;

import com.example.backend.dto.ScanResultResponse;
import com.example.backend.entity.Assessment;
import com.example.backend.entity.Enum.AssessmentStatus;
import com.example.backend.entity.Enum.AssessmentType;
import com.example.backend.entity.ScanResult;
import com.example.backend.repository.AssessmentRepository;
import com.example.backend.repository.ScanResultRepository;
import com.example.backend.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class ScanService {
    private final AssessmentRepository assessmentRepository;
    private final ScanResultRepository scanResultRepository;
    private final NmapXmlParser nmapXmlParser;
    private final GobusterParser gobusterParser;
    private final NiktoParser niktoParser;
    private final SslscanParser sslscanParser;
    private final WhatwebParser whatwebParser;
    private final Wafw00fParser wafw00fParser;
    private final DirsearchParser dirsearchParser;

    @Lazy
    @Autowired
    private ScanService self;

    private static final Map<AssessmentType, List<String>> TOOL_SETS = Map.of(
            AssessmentType.WEB, List.of(
                    "NMAP", "GOBUSTER", "NIKTO", "SSLSCAN",
                    "WHATWEB", "WAFW00F", "DIRSEARCH"
            ),
            AssessmentType.SERVER, List.of(
                    "NMAP"
            )
    );

    @Transactional
    public void startScan(Long assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay Assessment ID: " + assessmentId));
        if (assessment.getScopes().isEmpty()) {
            throw new IllegalArgumentException("Assessment chua co muc tieu (scope)");
        }

        assessment.setStatus(AssessmentStatus.SCANNING);
        assessmentRepository.save(assessment);

        String target = assessment.getScopes().get(0).getTarget();
        List<String> tools = TOOL_SETS.getOrDefault(assessment.getType(), List.of("NMAP"));

        for (String toolName : tools) {
            ScanResult scanResult = ScanResult.builder()
                    .assessment(assessment)
                    .toolName(toolName)
                    .target(target)
                    .status("RUNNING")
                    .build();
            scanResultRepository.save(scanResult);

            self.executeScanAsync(scanResult.getId(), assessment.getId(), toolName, target);
        }
    }

    @Async
    public void executeScanAsync(Long scanResultId, Long assessmentId,
                                 String toolName, String target) {
        try {
            List<String> command = buildCommand(toolName, target);
            log.info("Bat dau quet [{}] muc tieu: {}", toolName, target);
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);

            java.io.File tempFile = java.io.File.createTempFile("scan_result_", ".tmp");
            pb.redirectOutput(tempFile);

            Process process = pb.start();

            boolean finished = process.waitFor(900, TimeUnit.SECONDS);
            String output;
            if (!finished) {
                process.destroyForcibly();
                tempFile.delete();
                updateScanResult(scanResultId, assessmentId, null, "TIMEOUT");
                return;
            }
            output = java.nio.file.Files.readString(tempFile.toPath());
            tempFile.delete();

            if (process.exitValue() != 0) {
                updateScanResult(scanResultId, assessmentId, output, "FAILED");
                return;
            }
            updateScanResult(scanResultId, assessmentId, output, "SUCCESS");
        } catch (Exception e) {
            log.error("Loi khi quet [{}]: {}", toolName, e.getMessage(), e);
            updateScanResult(scanResultId, assessmentId, "ERROR: " + e.getMessage(), "FAILED");
        }
    }

    private List<String> buildCommand(String toolName, String target) {
        String script = switch (toolName) {
            case "NMAP"       -> "run_nmap.sh";
            case "GOBUSTER"   -> "run_gobuster.sh";
            case "NIKTO"      -> "run_nikto.sh";
            case "SSLSCAN"    -> "run_sslscan.sh";
            case "WHATWEB"    -> "run_whatweb.sh";
            case "WAFW00F"    -> "run_wafw00f.sh";
            case "DIRSEARCH"  -> "run_dirsearch.sh";
            default -> throw new IllegalArgumentException("Tool khong duoc ho tro: " + toolName);
        };
        return List.of("docker", "exec", "kali-worker", "bash", "/scripts/" + script, target);
    }

    @Transactional
    public void updateScanResult(Long scanResultId, Long assessmentId,
                                 String output, String status) {
        ScanResult result = scanResultRepository.findById(scanResultId).orElse(null);
        if (result != null) {
            result.setRawOutput(output);
            result.setStatus(status);
            result.setFinishedAt(LocalDateTime.now());

            if ("SUCCESS".equals(status) && output != null) {
                String parsedJson = parseOutput(result.getToolName(), output);
                result.setParsedData(parsedJson);
            }
            scanResultRepository.save(result);
        }

        checkAndUpdateAssessmentStatus(assessmentId);
        log.info("Quet xong [{}] tool={} - Trang thai: {}", scanResultId, result != null ? result.getToolName() : "?", status);
    }

    private void checkAndUpdateAssessmentStatus(Long assessmentId) {
        List<ScanResult> allResults = scanResultRepository.findByAssessmentId(assessmentId);
        boolean allDone = allResults.stream().noneMatch(r -> "RUNNING".equals(r.getStatus()));

        if (allDone) {
            Assessment assessment = assessmentRepository.findById(assessmentId).orElse(null);
            if (assessment != null) {
                assessment.setStatus(AssessmentStatus.SCAN_COMPLETED);
                assessmentRepository.save(assessment);
                log.info("Assessment {} - Tat ca tool da quet xong", assessmentId);
            }
        }
    }

    private String parseOutput(String toolName, String output) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return switch (toolName) {
                case "NMAP"      -> mapper.writeValueAsString(nmapXmlParser.parse(output));
                case "GOBUSTER"  -> mapper.writeValueAsString(gobusterParser.parse(output));
                case "NIKTO"     -> mapper.writeValueAsString(niktoParser.parse(output));
                case "SSLSCAN"   -> mapper.writeValueAsString(sslscanParser.parse(output));
                case "WHATWEB"   -> mapper.writeValueAsString(whatwebParser.parse(output));
                case "WAFW00F"   -> mapper.writeValueAsString(wafw00fParser.parse(output));
                case "DIRSEARCH" -> mapper.writeValueAsString(dirsearchParser.parse(output));
                default -> null;
            };
        } catch (Exception e) {
            log.error("Loi khi parse output cua {}: {}", toolName, e.getMessage());
            return null;
        }
    }

    @Transactional(readOnly = true)
    public List<ScanResultResponse> getResults(Long assessmentId) {
        return scanResultRepository.findByAssessmentId(assessmentId).stream()
                .map(r -> new ScanResultResponse(r.getId(), r.getToolName(), r.getTarget(), r.getRawOutput(), r.getParsedData(), r.getStatus(), r.getStartedAt(), r.getFinishedAt()))
                .toList();
    }
}
