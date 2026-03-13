
create table format_file(
    id UUID primary key,
    libelle VARCHAR(50) UNIQUE Not Null,
    extension varchar(30)

);

CREATE TABLE anomaly_type (
    id UUID PRIMARY KEY,
    wording VARCHAR(255) UNIQUE NOT NULL
);

create table analyzed_file(
    id UUID NOT NULL ,
    nom_fichier varchar(100) not null,
    upload_date timestamp not null,
    id_project UUID NOT NULL,
    format_id UUID NOT NULL,
    PRIMARY KEY (id_project, id),
    constraint fk_file_project foreign key (id_project) references projects(id),
    constraint fk_file_format foreign key (format_id) references format_file(id)
);


create table report(
    id UUID primary key,
    audit_date TIMESTAMP not null,
    id_project UUID NOT NULL,
    constraint fk_report_project foreign key (id_project) references projects(id)

);

create table anomaly(
    id UUID primary key,
    description TEXT NOT NULL,
    severity VARCHAR(100) NOT NULL,
    type_anomalie_id UUID NOT NULL,
    rapport_id UUID NOT NULL,
    CONSTRAINT fk_anomaly_type FOREIGN KEY (type_anomalie_id) REFERENCES anomaly_type(id),
    CONSTRAINT fk_anomaly_report FOREIGN KEY (rapport_id) REFERENCES report(id)

);
