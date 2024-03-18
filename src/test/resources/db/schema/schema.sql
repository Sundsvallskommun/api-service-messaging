
    create table history (
        created_at datetime(6),
        id bigint not null auto_increment,
        batch_id varchar(36),
        delivery_id varchar(36),
        message_id varchar(36) not null,
        party_id varchar(36),
        content LONGTEXT,
        department varchar(255),
        origin varchar(255),
        status_detail LONGTEXT,
        message_type enum ('MESSAGE','EMAIL','SMS','WEB_MESSAGE','DIGITAL_MAIL','DIGITAL_INVOICE','SNAIL_MAIL','LETTER','SLACK'),
        original_message_type enum ('MESSAGE','EMAIL','SMS','WEB_MESSAGE','DIGITAL_MAIL','DIGITAL_INVOICE','SNAIL_MAIL','LETTER','SLACK'),
        status enum ('PENDING','AWAITING_FEEDBACK','SENT','NOT_SENT','FAILED','NO_CONTACT_SETTINGS_FOUND','NO_CONTACT_WANTED'),
        primary key (id)
    ) engine=InnoDB;

    create table messages (
        created_at datetime(6),
        id bigint not null auto_increment,
        batch_id varchar(255),
        content LONGTEXT NOT NULL,
        delivery_id varchar(255),
        message_id varchar(255),
        party_id varchar(255),
        message_type enum ('MESSAGE','EMAIL','SMS','WEB_MESSAGE','DIGITAL_MAIL','DIGITAL_INVOICE','SNAIL_MAIL','LETTER','SLACK'),
        original_message_type enum ('MESSAGE','EMAIL','SMS','WEB_MESSAGE','DIGITAL_MAIL','DIGITAL_INVOICE','SNAIL_MAIL','LETTER','SLACK'),
        status enum ('PENDING','AWAITING_FEEDBACK','SENT','NOT_SENT','FAILED','NO_CONTACT_SETTINGS_FOUND','NO_CONTACT_WANTED'),
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

    create index idx_history_department
       on history (department);
