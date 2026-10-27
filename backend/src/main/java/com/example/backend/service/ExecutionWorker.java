package com.example.backend.service;

import com.example.backend.dto.ExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class ExecutionWorker {
    private static final long TIMEOUT_SECONDS = 300; // 5 phút
    /**
     * Thực thi script trong container kali-worker
     */
    public ExecutionResult execute(String toolName, String target) {
        String scriptName = "run_" + toolName.toLowerCase().trim() + ".sh";
        String command = String.format("docker exec kali-worker bash /scripts/%s %s", scriptName, target);
        log.info("ExecutionWorker bắt đầu chạy: {}", command);
        StringBuilder outputBuffer = new StringBuilder();
        boolean timedOut = false;
        int exitCode = -1;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
            processBuilder.redirectErrorStream(true); // Gộp stderr vào chung stdout
            Process process = processBuilder.start();
            // Đọc luồng output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputBuffer.append(line).append("\n");
                }
            }
            // Đợi tiến trình hoàn tất trong tối đa 300 giây
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                log.warn("Tiến trình quá thời gian cho phép ({}s). Đang cưỡng chế dừng...", TIMEOUT_SECONDS);
                process.destroyForcibly();
                timedOut = true;
                outputBuffer.append("\n[TIMEOUT] Lệnh bị dừng do vượt quá thời gian tối đa (300s).");
            } else {
                exitCode = process.exitValue();
                log.info("Tiến trình kết thúc với mã thoát: {}", exitCode);
            }
        } catch (Exception e) {
            log.error("Lỗi khi thực thi lệnh CLI: {}", e.getMessage(), e);
            outputBuffer.append("\n[LỖI HỆ THỐNG]: ").append(e.getMessage());
        }
        return new ExecutionResult(exitCode, outputBuffer.toString(), timedOut);
    }
}
