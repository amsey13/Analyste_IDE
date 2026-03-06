
create table support_projects(
    id UUID primary key references projects(id)
    --Maybe later there will be some atributes
);