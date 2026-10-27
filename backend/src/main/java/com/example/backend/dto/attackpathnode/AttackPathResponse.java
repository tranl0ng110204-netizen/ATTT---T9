package com.example.backend.dto.attackpathnode;

import java.util.List;

public record AttackPathResponse(
        Long assessmentId,
        String target,
        int totalSteps,
        List<AttackPathNode> nodes
) {
}
