ALTER TABLE `messages` ADD COLUMN `organization_number` varchar(12) DEFAULT NULL;
ALTER TABLE `history` ADD COLUMN `organization_number` varchar(12) DEFAULT NULL;

create index idx_history_organization_number
    on history (organization_number);
