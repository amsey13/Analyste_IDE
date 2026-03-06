Drop table if exists projects cascade;

create table projects(

    id UUID primary key ,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(1000),
    created_at TImestamp NOT NULL,
    updated_at TIMESTAMP,
    user_id UUID NOT NULL,
    project_type VARCHAR(30),
    constraint fk_project_user foreign key (user_id) references users

);