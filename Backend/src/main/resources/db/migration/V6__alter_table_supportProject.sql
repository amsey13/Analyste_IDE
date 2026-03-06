Drop table if exists support_projects cascade;


create table support_projects(
    id UUID primary key references projects(id),
    status VARCHAR(50) NOT NULL
);