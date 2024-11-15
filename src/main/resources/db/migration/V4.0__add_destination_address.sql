ALTER TABLE `messages` ADD COLUMN `destination_address` longtext DEFAULT NULL;
ALTER TABLE `history` ADD COLUMN `destination_address` longtext DEFAULT NULL;
