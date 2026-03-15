insert into format_file(id,libelle, extension) values (gen_random_UUID(),'BPMN','.bpmn'),
                                                   (gen_random_UUID(),'MCD','.mcd'),
                                                   (gen_random_UUID(),'MFC','.flu');

insert into anomaly_type(id,wording) values (gen_random_UUID(),'ACTEUR_PASSIF'),
                                         (gen_random_UUID(),'DONNEE_NON_MODÉLISE'),
                                         (gen_random_UUID(),'OBJECT_SANS_ATTRIBUT'),
                                         (gen_random_UUID(),'TACHE_SANS_US'),
                                         (gen_random_UUID(),'LIBELLE_NON_CONFORME'),
                                         (gen_random_UUID(),'INCOHERENCE_LOGIQUE'),
                                         (gen_random_UUID(),'REDONDANCE_SÉMANTIQUE'),
                                         (gen_random_UUID(),'IMPASSE_LOGIQUE');