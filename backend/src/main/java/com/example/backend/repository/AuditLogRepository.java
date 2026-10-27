package com.example.backend.repository;

import com.example.backend.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog,Long> {
    // Lấy toàn bộ log sắp xếp theo thời gian mới nhất lên đầu
    List<AuditLog> findAllByOrderByCreatedAtDesc();

    // Hỗ trợ lọc theo loại hành động
    List<AuditLog> findByActionTypeOrderByCreatedAtDesc(String actionType);
}
