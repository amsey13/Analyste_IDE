CREATE TABLE business_rules (
                                id UUID PRIMARY KEY,
                                code VARCHAR(50) NOT NULL,
                                description TEXT NOT NULL,
                                project_id UUID NOT NULL,
                                CONSTRAINT fk_business_rules_project FOREIGN KEY (project_id) REFERENCES support_project(id) ON DELETE CASCADE
);