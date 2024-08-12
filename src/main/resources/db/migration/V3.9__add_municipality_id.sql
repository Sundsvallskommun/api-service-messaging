alter table messages
    add column municipality_id varchar(255);

alter table history
    add column municipality_id varchar(255);

create index idx_history_municipality_id
    on history (municipality_id);
