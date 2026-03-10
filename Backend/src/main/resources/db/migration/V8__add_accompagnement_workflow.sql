-- 1. Ajout des colonnes pour le BPMN et le dictionnaire (Séparé pour compatibilité H2)
ALTER TABLE support_project ADD COLUMN bpmn_xml TEXT;
ALTER TABLE support_project ADD COLUMN data_dictionary TEXT;

-- 2. Création de la table 'actor'
CREATE TABLE actor (
                       id UUID PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       support_project_id UUID NOT NULL,
                       CONSTRAINT fk_actor_project FOREIGN KEY (support_project_id) REFERENCES support_project(id) ON DELETE CASCADE
);

-- 3. Création de la table 'user_story'
CREATE TABLE user_story (
                            id UUID PRIMARY KEY,
                            identifier VARCHAR(50) NOT NULL,
                            description VARCHAR(500) NOT NULL,
                            actor_id UUID NOT NULL,
                            support_project_id UUID NOT NULL,
                            CONSTRAINT fk_us_actor FOREIGN KEY (actor_id) REFERENCES actor(id) ON DELETE RESTRICT,
                            CONSTRAINT fk_us_project FOREIGN KEY (support_project_id) REFERENCES support_project(id) ON DELETE CASCADE
);