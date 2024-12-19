ALTER TABLE `messages` ADD COLUMN `destination_address` text DEFAULT NULL;
ALTER TABLE `history` ADD COLUMN `destination_address` text DEFAULT NULL;
