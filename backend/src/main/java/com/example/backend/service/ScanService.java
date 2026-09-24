package com.example.backend.service;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ScanService {
    public String scan(String target) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "docker",
                    "exec",
                    "kali-worker",
                    "nmap",
                    "-T4",
                    "-sV",
                    target
            );

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            boolean finished = process.waitFor(
                    30,
                    TimeUnit.SECONDS
            );

            if (!finished) {
                process.destroyForcibly();

                throw new RuntimeException(
                        "Nmap scan timeout after 30 seconds"
                );
            }

            String result = new String(
                    process.getInputStream().readAllBytes()
            );

            if (process.exitValue() != 0) {
                throw new RuntimeException(
                        "Nmap failed: " + result
                );
            }

            return result;

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Nmap scan interrupted",
                    e
            );

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Cannot execute Nmap: " + e.getMessage(),
                    e
            );
        }
    }
}
