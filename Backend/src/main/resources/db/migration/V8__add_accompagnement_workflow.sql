ALTER TABLE projects ADD COLUMN IF NOT EXISTS project_type VARCHAR(50);


ALTER TABLE support_project
    ADD COLUMN bpmn_xml TEXT,
ADD COLUMN data_dictionary TEXT;


CREATE TABLE actor (
                       id UUID PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       support_project_id UUID NOT NULL,
                       CONSTRAINT fk_actor_project FOREIGN KEY (support_project_id) REFERENCES support_project(id) ON DELETE CASCADE
);

CREATE TABLE user_story (
                            id UUID PRIMARY KEY,
                            identifier VARCHAR(50) NOT NULL,
                            description VARCHAR(500) NOT NULL,
                            actor_id UUID NOT NULL,
                            support_project_id UUID NOT NULL,
                            CONSTRAINT fk_us_actor FOREIGN KEY (actor_id) REFERENCES actor(id) ON DELETE RESTRICT,
                            CONSTRAINT fk_us_project FOREIGN KEY (support_project_id) REFERENCES support_project(id) ON DELETE CASCADE
);