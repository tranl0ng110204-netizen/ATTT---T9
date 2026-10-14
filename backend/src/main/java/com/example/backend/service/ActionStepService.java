package com.example.backend.service;

import com.example.backend.dto.ActionStepResponse;
import com.example.backend.dto.PolicyResult;
import com.example.backend.entity.ActionStep;
import com.example.backend.repository.ActionStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionStepService {
    private final ActionStepRepository actionStepRepository;
    private final PolicyEngine policyEngine;

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
        action.setStatus("APPROVED");
        ActionStep saved = actionStepRepository.save(action);
        log.info("Hành động ID {} (Tool: {}) đã được phê duyệt (APPROVED)!", saved.getId(), saved.getToolName());

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
}
