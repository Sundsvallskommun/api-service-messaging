ALTER TABLE `history` DROP PRIMARY KEY;
ALTER TABLE `history` ADD COLUMN `id` INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY;
ALTER TABLE `history` ADD COLUMN `delivery_id` VARCHAR(36) NOT NULL DEFAULT UUID();

ALTER TABLE `messages` DROP PRIMARY KEY;
ALTER TABLE `messages` ADD COLUMN `id` INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY;
ALTER TABLE `messages` ADD COLUMN `delivery_id` VARCHAR(36) NOT NULL DEFAULT UUID();
