ALTER TABLE dictionary_attribute ALTER COLUMN entry_id DROP NOT NULL;

ALTER TABLE dictionary_attribute ADD COLUMN association_id UUID;


ALTER TABLE dictionary_attribute
    ADD CONSTRAINT fk_attr_association
        FOREIGN KEY (association_id) REFERENCES dictionary_associations(id) ON DELETE CASCADE;