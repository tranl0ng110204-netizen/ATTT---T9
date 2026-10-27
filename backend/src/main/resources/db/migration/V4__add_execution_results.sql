-- Bảng lưu trữ kết quả thực thi terminal của các Action Step
CREATE TABLE execution_results (
                                   id SERIAL PRIMARY KEY,
                                   action_step_id BIGINT NOT NULL REFERENCES action_steps(id) ON DELETE CASCADE,
                                   exit_code INT NOT NULL,
                                   raw_output TEXT,
                                   timed_out BOOLEAN DEFAULT FALSE,
                                   started_at TIMESTAMP,
                                   finished_at TIMESTAMP,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_execution_results_action_id ON execution_results(action_step_id);