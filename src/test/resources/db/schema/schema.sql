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

create index idx_history_batch_id
    on history (batch_id);

create index idx_history_message_id
    on history (message_id);

create index idx_history_delivery_id
    on history (delivery_id);

create index idx_history_origin
    on history (origin);

create index idx_history_issuer
    on history (issuer);

create index idx_history_department
    on history (department);

create index idx_history_municipality_id
    on history (municipality_id);

create index idx_history_organization_number
    on history (organization_number);
