
ALTER TABLE user_story
    ADD COLUMN benefit VARCHAR(500);

ALTER TABLE user_story
    ADD COLUMN acceptance_criteria TEXT;

ALTER TABLE user_story
    ADD CONSTRAINT uk_user_story_identifier UNIQUE (identifier);