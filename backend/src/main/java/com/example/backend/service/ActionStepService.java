package com.example.backend.service;

import com.example.backend.dto.ActionStepResponse;
import com.example.backend.dto.ExecutionLogResponse;
import com.example.backend.dto.ExecutionResult;
import com.example.backend.dto.PolicyResult;
import com.example.backend.entity.ActionStep;
import com.example.backend.entity.ExecutionLog;
import com.example.backend.repository.ActionStepRepository;
import com.example.backend.repository.ExecutionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionStepService {
    private final ActionStepRepository actionStepRepository;
    private final PolicyEngine policyEngine;
    private final ExecutionWorker executionWorker;
    private final ExecutionLogRepository executionLogRepository;
    private final AuditLogService auditLogService;

    /**
     * Kiểm tra trạng thái Policy của một Action
     */
    public PolicyResult checkPolicy(Long actionId) {
        ActionStep action = actionStepRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Action Step ID: " + actionId));
        return policyEngine.validate(action);
    }

    @Transactional
    public ActionStepResponse approveAction(Long actionId){
        ActionStep action = actionStepRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot find actionStepId: " + actionId));
        if(!"PROPOSED".equalsIgnoreCase(action.getStatus())){
            throw new IllegalStateException(" Only approve action not in PROPOSED state . " +
                    "Current action is :" + action.getStatus());
        }

        // Kiểm tra Policy Engine
        PolicyResult policyResult = policyEngine.validate(action);
        if (!policyResult.passed()) {
            String errorMsg = String.join(", ", policyResult.violations());
            // GHI NHẬN VẾT VI PHẠM POLICY VÀO AUDIT LOG
            auditLogService.record("POLICY_BLOCK", "admin", action.getTarget(), "VIOLATION",
                    "Hành động ID " + actionId + " (" + action.getToolName() + ") bị chặn: " + errorMsg);
            throw new IllegalArgumentException("Từ chối phê duyệt do vi phạm chính sách an toàn: " + errorMsg);
        }


        action.setStatus("APPROVED");
        ActionStep saved = actionStepRepository.save(action);
        log.info("Hành động ID {} (Tool: {}) đã được phê duyệt (APPROVED)!", saved.getId(), saved.getToolName());

        // GHI NHẬN VẾT PHÊ DUYỆT THÀNH CÔNG
        auditLogService.record("APPROVE_ACTION", "admin", action.getTarget(), "SUCCESS",
                "Phê duyệt công cụ " + action.getToolName() + " cho Action ID " + actionId + ". Lý do: " + action.getReason());
        return mapToResponse(saved);

    }

    @Transactional
    public ActionStepResponse rejectAction(Long actionId){
        ActionStep action = actionStepRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot find actionStepId: " + actionId));
        if(!"PROPOSED".equalsIgnoreCase(action.getStatus())){
            throw new IllegalStateException(" Only approve action not in PROPOSED state . " +
                    "Current action is :" + action.getStatus());
        }
        action.setStatus("REJECTED");
        ActionStep saved = actionStepRepository.save(action);
        log.info("Hành động ID {} (Tool: {}) đã bị từ chối (REJECTED)!", saved.getId(), saved.getToolName());


        // GHI NHẬN VẾT TỪ CHỐI
        auditLogService.record("REJECT_ACTION", "admin", action.getTarget(), "SUCCESS",
                "Người dùng đã từ chối chạy công cụ " + action.getToolName() + " (Action ID: " + actionId + ")");
        return mapToResponse(saved);
    }

    private ActionStepResponse mapToResponse(ActionStep a){
        return new ActionStepResponse(
                a.getId(),
                a.getToolName(),
                a.getTarget(),
                a.getReason(),
                a.getStatus(),
                a.getCreatedAt()
        );
    }

    @Transactional
    public ExecutionResult executeAction(Long actionId){
        ActionStep action = actionStepRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Action Step ID: " + actionId));
        if (!"APPROVED".equalsIgnoreCase(action.getStatus())) {
            throw new IllegalStateException("Chỉ có thể thực thi hành động đã được phê duyệt (APPROVED)! Trạng thái hiện tại: " + action.getStatus());
        }
        // Cập nhật trạng thái sang đang chạy
        action.setStatus("EXECUTING");
        actionStepRepository.save(action);
        log.info("Bắt đầu thực thi Action ID: {} - Tool: {} - Target: {}", action.getId(), action.getToolName(), action.getTarget());

        LocalDateTime startedAt = LocalDateTime.now();

        // Chạy qua ExecutionWorker
        ExecutionResult result = executionWorker.execute(action.getToolName(), action.getTarget());
        // Cập nhật trạng thái sau khi xong

        LocalDateTime finishedAt = LocalDateTime.now();

        if (result.timedOut() || result.exitCode() != 0) {
            action.setStatus("FAILED");
        } else {
            action.setStatus("COMPLETED");
        }
        actionStepRepository.save(action);

        ExecutionLog logEntity = ExecutionLog.builder()
                .actionStep(action)
                .exitCode(result.exitCode())
                .rawOutput(result.output())
                .timedOut(result.timedOut())
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .build();
        executionLogRepository.save(logEntity);
        log.info("Đã lưu Execution Log cho Action ID: {} vào CSDL thành công!", action.getId());


        String execStatus = (result.timedOut() || result.exitCode() != 0) ? "FAILED" : "SUCCESS";
        auditLogService.record("EXECUTE_ACTION", "admin", action.getTarget(), execStatus,
                "Chạy công cụ " + action.getToolName() + " (Exit code: " + result.exitCode() + ", TimedOut: " + result.timedOut() + ")");

        return result;
    }

    /**
     * Lấy log thực thi đã lưu của một ActionStep
     */
    public ExecutionLogResponse getExecutionResult(Long actionId) {
        ExecutionLog logEntity = executionLogRepository.findTopByActionStepIdOrderByIdDesc(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Chưa có kết quả thực thi cho Action Step ID: " + actionId));
        return new ExecutionLogResponse(
                logEntity.getId(),
                logEntity.getActionStep().getId(),
                logEntity.getExitCode(),
                logEntity.getRawOutput(),
                logEntity.isTimedOut(),
                logEntity.getStartedAt(),
                logEntity.getFinishedAt()
        );
    }
}
