
ALTER TABLE user_story DROP CONSTRAINT fk_us_actor;

ALTER TABLE user_story ADD CONSTRAINT fk_us_actor FOREIGN KEY (actor_id) REFERENCES actor(id) ON DELETE CASCADE;

ALTER TABLE support_project DROP COLUMN data_dictionary;


CREATE TABLE dictionary_entry (
                                  id UUID PRIMARY KEY,
                                  name VARCHAR(255) NOT NULL,
                                  description TEXT,
                                  project_id UUID NOT NULL,
                                  CONSTRAINT fk_dict_entry_project FOREIGN KEY (project_id) REFERENCES support_project(id) ON DELETE CASCADE
);

CREATE TABLE dictionary_attribute (
                                      id UUID PRIMARY KEY,
                                      name VARCHAR(255) NOT NULL,
                                      data_type VARCHAR(50) NOT NULL,
                                      size VARCHAR(50),
                                      is_primary_key BOOLEAN DEFAULT FALSE,
                                      is_not_null BOOLEAN DEFAULT FALSE,
                                      description TEXT,
                                      entry_id UUID NOT NULL,
                                      CONSTRAINT fk_dict_attr_entry FOREIGN KEY (entry_id) REFERENCES dictionary_entry(id) ON DELETE CASCADE
);