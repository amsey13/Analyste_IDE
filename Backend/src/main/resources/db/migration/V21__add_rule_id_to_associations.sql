ALTER TABLE dictionary_associations
    ADD COLUMN rule_id UUID;

ALTER TABLE dictionary_associations
    ADD CONSTRAINT fk_assoc_rule FOREIGN KEY (rule_id) REFERENCES business_rules(id) ON DELETE SET NULL;