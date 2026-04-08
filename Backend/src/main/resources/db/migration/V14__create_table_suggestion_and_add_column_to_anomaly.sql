create table suggestion(
    id UUID primary key ,
    content TEXT NOT NULL,
    anomaly_id UUID NOT NULL,
    constraint fk_suggestion_anomaly foreign key (anomaly_id) references anomaly(id) on delete cascade
);

