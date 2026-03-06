
create table audit_project(

    id UUID primary key references projects(id),
    taiga_token TEXT,
    project_slug VARCHAR(255)

);