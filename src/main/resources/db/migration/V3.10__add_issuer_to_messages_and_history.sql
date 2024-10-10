ALTER TABLE `messages` 
    ADD COLUMN `issuer` varchar(255) DEFAULT NULL;

ALTER TABLE `history` 
    ADD COLUMN `issuer` varchar(255) DEFAULT NULL;

create index idx_history_issuer
    on history (issuer);
