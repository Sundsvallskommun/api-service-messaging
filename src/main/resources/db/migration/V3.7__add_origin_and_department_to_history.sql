ALTER TABLE `history` ADD COLUMN `origin` varchar(255) DEFAULT NULL;
ALTER TABLE `history` ADD COLUMN `department` varchar(255) DEFAULT NULL;

ALTER TABLE `history` ADD INDEX `idx_history_origin`(`origin`);
ALTER TABLE `history` ADD INDEX `idx_history_department`(`department`);
