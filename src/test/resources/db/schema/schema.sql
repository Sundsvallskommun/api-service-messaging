
create table history (
                         created_at datetime(6),
                         id bigint not null auto_increment,
                         organization_number varchar(12),
                         batch_id varchar(36),
                         delivery_id varchar(36),
                         message_id varchar(36) not null,
                         party_id varchar(36),
                         content LONGTEXT,
                         department varchar(255),
                         destination_address varchar(255),
                         issuer varchar(255),
                         municipality_id varchar(255),
                         origin varchar(255),
                         status_detail LONGTEXT,
                         message_type enum ('DIGITAL_INVOICE','DIGITAL_MAIL','EMAIL','LETTER','MESSAGE','SLACK','SMS','SNAIL_MAIL','WEB_MESSAGE'),
                         original_message_type enum ('DIGITAL_INVOICE','DIGITAL_MAIL','EMAIL','LETTER','MESSAGE','SLACK','SMS','SNAIL_MAIL','WEB_MESSAGE'),
                         status enum ('AWAITING_FEEDBACK','FAILED','NOT_SENT','NO_CONTACT_SETTINGS_FOUND','NO_CONTACT_WANTED','PENDING','SENT'),
                         primary key (id)
) engine=InnoDB;

create table messages (
                          created_at datetime(6),
                          id bigint not null auto_increment,
                          organization_number varchar(12),
                          batch_id varchar(255),
                          content LONGTEXT NOT NULL,
                          delivery_id varchar(255),
                          destination_address varchar(255),
                          issuer varchar(255),
                          message_id varchar(255),
                          municipality_id varchar(255),
                          origin varchar(255),
                          party_id varchar(255),
                          message_type enum ('DIGITAL_INVOICE','DIGITAL_MAIL','EMAIL','LETTER','MESSAGE','SLACK','SMS','SNAIL_MAIL','WEB_MESSAGE'),
                          original_message_type enum ('DIGITAL_INVOICE','DIGITAL_MAIL','EMAIL','LETTER','MESSAGE','SLACK','SMS','SNAIL_MAIL','WEB_MESSAGE'),
                          status enum ('AWAITING_FEEDBACK','FAILED','NOT_SENT','NO_CONTACT_SETTINGS_FOUND','NO_CONTACT_WANTED','PENDING','SENT'),
                          primary key (id)
) engine=InnoDB;

create index idx_history_municipality_issuer_created
    on history (municipality_id, issuer, created_at);

create index idx_history_municipality_party_created
    on history (municipality_id, party_id, created_at);

create index idx_history_municipality_message
    on history (municipality_id, message_id);

create index idx_history_municipality_batch
    on history (municipality_id, batch_id);

create index idx_history_municipality_delivery
    on history (municipality_id, delivery_id);

create index idx_history_municipality_batch_issuer_created
    on history (municipality_id, batch_id, issuer, created_at);

create index idx_history_municipality_message_type
    on history (municipality_id, message_id, message_type);

create index idx_history_municipality_message_issuer
    on history (municipality_id, message_id, issuer);

create index idx_history_created_at
    on history (created_at);

create index idx_history_origin
    on history (origin);

create index idx_history_department
    on history (department);

create index idx_history_organization_number
    on history (organization_number);
