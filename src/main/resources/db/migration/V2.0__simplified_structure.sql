/*
CREATE TABLE `history` (
    `message_id` varchar(36) NOT NULL,
    `batch_id` varchar(36) DEFAULT NULL,
    `party_id` varchar(36) DEFAULT NULL,
    `message_type` varchar(16) DEFAULT NULL,
    `content` longtext DEFAULT NULL,
    `status` varchar(32) DEFAULT NULL,
    `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
*/

ALTER TABLE `history` DROP PRIMARY KEY;
ALTER TABLE `history` ADD PRIMARY KEY(`message_id`);
ALTER TABLE `history` MODIFY COLUMN `message_id` varchar(36) NOT NULL;
ALTER TABLE `history` MODIFY COLUMN `batch_id` varchar(36) DEFAULT NULL;
ALTER TABLE `history` MODIFY COLUMN `party_id` varchar(36) DEFAULT NULL;
ALTER TABLE `history` MODIFY COLUMN `message_type` varchar(16) DEFAULT NULL;
ALTER TABLE `history` ADD COLUMN `content` longtext DEFAULT NULL;
ALTER TABLE `history` MODIFY COLUMN `status` varchar(32) DEFAULT NULL;
ALTER TABLE `history` MODIFY COLUMN `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE `history` DROP COLUMN `id`;
ALTER TABLE `history` DROP COLUMN `message`;
ALTER TABLE `history` DROP COLUMN `party_contact`;
ALTER TABLE `history` DROP COLUMN `sender`;

/*
CREATE TABLE `messages` (
    `message_id` varchar(32) NOT NULL,
    `batch_id` varchar(32) DEFAULT NULL,
    `party_id` varchar(32) DEFAULT NULL,
    `message_type` varchar(16) DEFAULT NULL,
    `content` longtext NOT NULL,
    `status` varchar(32) DEFAULT NULL,
    `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
*/

ALTER TABLE `messages` MODIFY COLUMN `message_id` varchar(36) NOT NULL;
ALTER TABLE `messages` MODIFY COLUMN `batch_id` varchar(36) DEFAULT NULL;
ALTER TABLE `messages` MODIFY COLUMN `party_id` varchar(36) DEFAULT NULL;
ALTER TABLE `messages` MODIFY COLUMN `message_type` varchar(16) DEFAULT NULL;
ALTER TABLE `messages` ADD COLUMN `content` longtext DEFAULT NULL;
ALTER TABLE `messages` CHANGE COLUMN `message_status` `status` VARCHAR(32) DEFAULT NULL;
ALTER TABLE `messages` ADD COLUMN `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE `messages` DROP COLUMN `email_name`;
ALTER TABLE `messages` DROP COLUMN `sms_name`;
ALTER TABLE `messages` DROP COLUMN `sender_email`;
ALTER TABLE `messages` DROP COLUMN `subject`;
ALTER TABLE `messages` DROP COLUMN `message`;
