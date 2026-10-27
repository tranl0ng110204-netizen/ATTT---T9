package com.example.backend.repository;

import com.example.backend.entity.ExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExecutionLogRepository extends JpaRepository<ExecutionLog,Long> {
    // Lấy log thực thi mới nhất của một ActionStep
    Optional<ExecutionLog> findTopByActionStepIdOrderByIdDesc(Long actionStepId);
}

