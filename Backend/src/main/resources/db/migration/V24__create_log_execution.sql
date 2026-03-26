CREATE TABLE log_execution (
                               id UUID PRIMARY KEY,
                               operation VARCHAR(50) NOT NULL,
                               start_time TIMESTAMP NOT NULL,
                               end_time TIMESTAMP NOT NULL,
                               duration_ms BIGINT NOT NULL,
                               status VARCHAR(20) NOT NULL,
                               details TEXT,
                               project_id UUID REFERENCES projects(id)
);