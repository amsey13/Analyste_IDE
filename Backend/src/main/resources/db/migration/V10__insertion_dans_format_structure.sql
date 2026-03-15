INSERT INTO format_file (id, libelle, extension) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'BPMN', '.bpmn'),
('67c8b773-ca72-466d-8898-0c624f114c00', 'MCD', '.mcd'),
('7b3c1044-67d1-4195-8e2b-f8973685e123', 'MFC', '.flu');

INSERT INTO anomaly_type (id, wording) VALUES
                                           ('a1b2c3d4-0001-47a8-b9c0-d1e2f3a4b5c6', 'ACTEUR_PASSIF'),
                                           ('a1b2c3d4-0002-47a8-b9c0-d1e2f3a4b5c6', 'DONNEE_NON_MODÉLISE'),
                                           ('a1b2c3d4-0003-47a8-b9c0-d1e2f3a4b5c6', 'OBJECT_SANS_ATTRIBUT'),
                                           ('b8c6a320-cd4f-46af-9b1e-4ed6e5093919', 'TACHE_SANS_US'),
                                           ('a1b2c3d4-0005-47a8-b9c0-d1e2f3a4b5c6', 'LIBELLE_NON_CONFORME'),
                                           ('a1b2c3d4-0006-47a8-b9c0-d1e2f3a4b5c6', 'INCOHERENCE_LOGIQUE'),
                                           ('a1b2c3d4-0007-47a8-b9c0-d1e2f3a4b5c6', 'REDONDANCE_SÉMANTIQUE'),
                                           ('a1b2c3d4-0008-47a8-b9c0-d1e2f3a4b5c6', 'IMPASSE_LOGIQUE');