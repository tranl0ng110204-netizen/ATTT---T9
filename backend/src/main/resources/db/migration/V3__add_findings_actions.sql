-- Bảng lưu trữ Lỗ hổng (Findings)
CREATE TABLE findings (
                          id SERIAL PRIMARY KEY,
                          assessment_id BIGINT REFERENCES assessments(id) ON DELETE CASCADE,
                          title VARCHAR(255) NOT NULL,
                          severity VARCHAR(50) NOT NULL,
                          description TEXT,
                          tool VARCHAR(100),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng lưu trữ Kế hoạch tấn công tiếp theo (Action Steps)
CREATE TABLE action_steps (
                              id SERIAL PRIMARY KEY,
                              assessment_id BIGINT REFERENCES assessments(id) ON DELETE CASCADE,
                              tool_name VARCHAR(100) NOT NULL,
                              target VARCHAR(255) NOT NULL,
                              reason TEXT,
                              status VARCHAR(50) DEFAULT 'PROPOSED', -- PROPOSED, APPROVED, REJECTED, COMPLETED
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);