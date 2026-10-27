package com.example.backend.service;

import com.example.backend.dto.AuditLogResponse;
import com.example.backend.entity.AuditLog;
import com.example.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    /**
     * Ghi nhận 1 sự kiện kiểm toán vào CSDL
     */
    @Transactional
    public void record(String actionType, String actor, String target, String status, String details) {
        AuditLog auditLog = AuditLog.builder()
                .actionType(actionType)
                .actor(actor != null ? actor : "admin")
                .target(target)
                .status(status)
                .details(details)
                .build();
        auditLogRepository.save(auditLog);
        log.info("[AUDIT] Hành động: {} | Actor: {} | Mục tiêu: {} | Trạng thái: {}", actionType, actor, target, status);
    }
    /**
     * Lấy danh sách audit log (có hỗ trợ lọc theo actionType nếu có)
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLogs(String actionType) {
        List<AuditLog> logs = (actionType == null || actionType.isBlank())
                ? auditLogRepository.findAllByOrderByCreatedAtDesc()
                : auditLogRepository.findByActionTypeOrderByCreatedAtDesc(actionType.toUpperCase().trim());
        return logs.stream()
                .map(l -> new AuditLogResponse(
                        l.getId(),
                        l.getActionType(),
                        l.getActor(),
                        l.getTarget(),
                        l.getStatus(),
                        l.getDetails(),
                        l.getCreatedAt()
                ))
                .toList();
    }
}
