CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       full_name VARCHAR(255),
                       external_id VARCHAR(255) UNIQUE,
                       is_active BOOLEAN DEFAULT TRUE
);