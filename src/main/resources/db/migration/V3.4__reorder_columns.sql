ALTER TABLE `messages` MODIFY COLUMN `id` INT(11) NOT NULL AUTO_INCREMENT FIRST;
ALTER TABLE `messages` MODIFY COLUMN `batch_id` VARCHAR(36) DEFAULT NULL AFTER `id`;
ALTER TABLE `messages` MODIFY COLUMN `message_id` VARCHAR(36) NOT NULL AFTER `batch_id`;
ALTER TABLE `messages` MODIFY COLUMN `delivery_id` VARCHAR(36) NOT NULL AFTER `message_id`;
ALTER TABLE `messages` MODIFY COLUMN `message_type` VARCHAR(16) DEFAULT NULL AFTER `delivery_id`;
ALTER TABLE `messages` MODIFY COLUMN `original_message_type` VARCHAR(16) DEFAULT NULL AFTER `message_type`;
ALTER TABLE `messages` MODIFY COLUMN `status` VARCHAR(32) DEFAULT NULL AFTER `original_message_type`;
ALTER TABLE `messages` MODIFY COLUMN `party_id` VARCHAR(36) DEFAULT NULL AFTER `status`;
ALTER TABLE `messages` MODIFY COLUMN `party_id` VARCHAR(36) DEFAULT NULL AFTER `status`;
ALTER TABLE `messages` MODIFY COLUMN `content` longtext DEFAULT NULL AFTER `party_id`;

ALTER TABLE `history` MODIFY COLUMN `id` INT(11) NOT NULL AUTO_INCREMENT FIRST;
ALTER TABLE `history` MODIFY COLUMN `batch_id` VARCHAR(36) DEFAULT NULL AFTER `id`;
ALTER TABLE `history` MODIFY COLUMN `message_id` VARCHAR(36) NOT NULL AFTER `batch_id`;
ALTER TABLE `history` MODIFY COLUMN `delivery_id` VARCHAR(36) NOT NULL AFTER `message_id`;
ALTER TABLE `history` MODIFY COLUMN `message_type` VARCHAR(16) DEFAULT NULL AFTER `delivery_id`;
ALTER TABLE `history` MODIFY COLUMN `original_message_type` VARCHAR(16) DEFAULT NULL AFTER `message_type`;
ALTER TABLE `history` MODIFY COLUMN `status` VARCHAR(32) DEFAULT NULL AFTER `original_message_type`;
ALTER TABLE `history` MODIFY COLUMN `party_id` VARCHAR(36) DEFAULT NULL AFTER `status`;
ALTER TABLE `history` MODIFY COLUMN `content` longtext DEFAULT NULL AFTER `party_id`;
