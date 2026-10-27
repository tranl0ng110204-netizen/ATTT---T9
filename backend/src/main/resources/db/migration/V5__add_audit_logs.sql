-- Bảng lưu trữ nhật ký kiểm toán (Audit Log)
CREATE TABLE audit_logs (
                            id SERIAL PRIMARY KEY,
                            action_type VARCHAR(100) NOT NULL, -- APPROVE, REJECT, EXECUTE, AI_ANALYZE, POLICY_BLOCK
                            actor VARCHAR(100) DEFAULT 'admin',  -- Người thực hiện (admin, security_operator...)
                            target VARCHAR(255),                 -- Mục tiêu bị tác động
                            status VARCHAR(50) NOT NULL,         -- SUCCESS, FAILED, VIOLATION
                            details TEXT,                        -- Chi tiết payload, lý do, hoặc kết quả
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_logs_action_type ON audit_logs(action_type);