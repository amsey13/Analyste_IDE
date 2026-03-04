create table projets(

     id_projet UUID PRIMARY KEY,
     date_creation TIMESTAMP,
     date_derniere_modification TIMESTAMP,
     description VARCHAR(1000),
     nom VARCHAR(255) NOT NULL,
     user_id UUID NOT NULL,
     taiga_token TEXT,
     slug_project VARCHAR(255),
     CONSTRAINT fk_projet_user FOREIGN KEY (user_id) REFERENCES users(id)

);