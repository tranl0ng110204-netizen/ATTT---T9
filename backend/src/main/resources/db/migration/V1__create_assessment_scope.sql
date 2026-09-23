CREATE TABLE assessment (
                            id BIGSERIAL PRIMARY KEY,

                            name VARCHAR(255) NOT NULL,

                            target_type VARCHAR(20) NOT NULL,

                            status VARCHAR(30) NOT NULL DEFAULT 'CREATED',

                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE scope (
                       id BIGSERIAL PRIMARY KEY,

                       assessment_id BIGINT NOT NULL,

                       target VARCHAR(500) NOT NULL,

                       target_type VARCHAR(20) NOT NULL,

                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_scope_assessment
                           FOREIGN KEY (assessment_id)
                               REFERENCES assessment(id)
                               ON DELETE CASCADE
);

CREATE INDEX idx_scope_assessment_id
    ON scope(assessment_id);