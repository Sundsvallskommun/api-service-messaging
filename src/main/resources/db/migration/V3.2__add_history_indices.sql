ALTER TABLE `history` ADD INDEX `idx_history_created_at`(`created_at`);
ALTER TABLE `history` ADD INDEX `idx_history_message_type`(`message_type`);
ALTER TABLE `history` ADD INDEX `idx_history_original_message_type`(`original_message_type`);
ALTER TABLE `history` ADD INDEX `idx_history_status`(`status`);
