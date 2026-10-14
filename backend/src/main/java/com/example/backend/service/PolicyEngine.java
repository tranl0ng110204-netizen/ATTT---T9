package com.example.backend.service;


import com.example.backend.dto.PolicyResult;
import com.example.backend.entity.ActionStep;
import com.example.backend.entity.Assessment;
import com.example.backend.entity.Scope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PolicyEngine {
    private final Set<String> allowedTools;


    public PolicyEngine(@Value("${security.policy.allowed-tools}")String allowedToolsStr) {
        this.allowedTools = Arrays.stream(allowedToolsStr.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    /**
     * Kiểm tra ActionStep có tuân thủ chính sách an toàn không
     */

    public PolicyResult validate(ActionStep action){
        List<String> violations = new ArrayList<>();
        Assessment assessment = action.getAssessment();

        // 1. Kiểm tra Tool Allowlist
        String tool = action.getToolName() == null ? "" : action.getToolName().trim().toLowerCase();
        if(!allowedTools.contains(tool)){
            violations.add("Công cụ '" + action.getToolName() +
                    "' không nằm trong danh sách cho phép (Allowed: " + allowedTools + ")");
        }

        // 2. Kiểm tra Scope (Target có nằm trong phạm vi cho phép không)
        if(assessment != null && assessment.getScopes() != null && !assessment.getScopes().isEmpty()){
            boolean inScope = false;
            String actionHost = extractHost(action.getTarget());

            for (Scope scope : assessment.getScopes()) {
                String scopeHost = extractHost(scope.getTarget());
                // Khớp host (ví dụ demo.testfire.net khớp với demo.testfire.net)
                if (actionHost.equalsIgnoreCase(scopeHost) || action.getTarget().contains(scopeHost)) {
                    inScope = true;
                    break;
                }
            }
            if (!inScope) {
                violations.add("Mục tiêu '" + action.getTarget() + "' nằm ngoài phạm vi (Scope) của bài kiểm thử!");
            }
        }

        if (!violations.isEmpty()) {
            log.warn("Policy Engine phát hiện vi phạm đối với Action ID {}: {}", action.getId(), violations);
            return PolicyResult.fail(violations);
        }
        log.info("Action ID {} đã vượt qua kiểm tra của Policy Engine!", action.getId());
        return PolicyResult.success();

    }
    /**
     * Hàm phụ trợ bóc tách Host/Domain từ URL hoặc IP
     */
    private String extractHost(String rawTarget) {
        if (rawTarget == null || rawTarget.isBlank()) return "";
        try {
            String target = rawTarget.trim();
            if (!target.startsWith("http://") && !target.startsWith("https://")) {
                target = "http://" + target;
            }
            URI uri = new URI(target);
            String host = uri.getHost();
            return host != null ? host : rawTarget;
        } catch (Exception e) {
            return rawTarget;
        }
    }



}
