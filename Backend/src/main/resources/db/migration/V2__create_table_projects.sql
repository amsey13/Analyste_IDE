create table projects(
     id UUID PRIMARY KEY,
     name VARCHAR(255) NOT NULL,
     description VARCHAR(1000),
     created_at TIMESTAMP NOT NULL,
     updated_at TIMESTAMP,
     user_id UUID NOT NULL,
     taiga_token TEXT,
     project_slug VARCHAR(255),
     CONSTRAINT fk_project_user FOREIGN KEY (user_id) REFERENCES users(id)
);