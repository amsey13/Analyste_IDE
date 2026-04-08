CREATE TABLE dictionary_associations (
                                         id UUID PRIMARY KEY,
                                         source_entry_id UUID NOT NULL,
                                         target_entry_id UUID NOT NULL,
                                         name VARCHAR(255) NOT NULL,
                                         source_multiplicity VARCHAR(10) NOT NULL,
                                         target_multiplicity VARCHAR(10) NOT NULL,
                                         CONSTRAINT fk_association_source
                                             FOREIGN KEY (source_entry_id)
                                                 REFERENCES dictionary_entry(id)
                                                 ON DELETE CASCADE,

                                         CONSTRAINT fk_association_target
                                             FOREIGN KEY (target_entry_id)
                                                 REFERENCES dictionary_entry(id)
                                                 ON DELETE CASCADE
);
CREATE INDEX idx_assoc_source ON dictionary_associations(source_entry_id);
CREATE INDEX idx_assoc_target ON dictionary_associations(target_entry_id);