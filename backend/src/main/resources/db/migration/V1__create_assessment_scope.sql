CREATE TABLE assessments (
                             id BIGSERIAL PRIMARY KEY,

                             name VARCHAR(255) NOT NULL,

                             type VARCHAR(20) NOT NULL,

                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT chk_assessment_type
                                 CHECK (type IN ('SERVER', 'WEB'))
);


CREATE TABLE scopes (
                        id BIGSERIAL PRIMARY KEY,

                        assessment_id BIGINT NOT NULL,

                        target VARCHAR(500) NOT NULL,

                        target_type VARCHAR(20) NOT NULL,

                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT fk_scope_assessment
                            FOREIGN KEY (assessment_id)
                                REFERENCES assessments(id)
                                ON DELETE CASCADE,

                        CONSTRAINT chk_scope_target_type
                            CHECK (target_type IN ('IP', 'CIDR', 'URL'))
);


CREATE INDEX idx_scopes_assessment_id
    ON scopes(assessment_id);