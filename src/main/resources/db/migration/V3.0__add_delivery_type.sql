ALTER TABLE `messages` ADD COLUMN `delivery_type` varchar(16) DEFAULT NULL;
ALTER TABLE `history` ADD COLUMN `delivery_type` varchar(16) DEFAULT NULL;
