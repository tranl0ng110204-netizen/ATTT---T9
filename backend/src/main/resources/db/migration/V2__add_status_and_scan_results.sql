-- Thêm cột status vào bảng assessments
ALTER TABLE assessments
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'CREATED';
ALTER TABLE assessments
    ADD CONSTRAINT chk_assessment_status
        CHECK (status IN ('CREATED', 'SCANNING', 'SCAN_COMPLETED',
                          'ANALYZING', 'PENDING_APPROVAL', 'COMPLETED'));
-- Tạo bảng lưu kết quả quét
CREATE TABLE scan_results (
                              id          BIGSERIAL PRIMARY KEY,
                              assessment_id BIGINT NOT NULL,
                              tool_name   VARCHAR(50) NOT NULL,
                              target      VARCHAR(500) NOT NULL,
                              raw_output  TEXT,
                              parsed_data TEXT,
                              status      VARCHAR(20) NOT NULL DEFAULT 'RUNNING',
                              started_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              finished_at TIMESTAMP,
                              CONSTRAINT fk_scan_assessment
                                  FOREIGN KEY (assessment_id) REFERENCES assessments(id) ON DELETE CASCADE,
                              CONSTRAINT chk_scan_status
                                  CHECK (status IN ('RUNNING', 'SUCCESS', 'FAILED', 'TIMEOUT'))
);
CREATE INDEX idx_scan_results_assessment_id ON scan_results(assessment_id);