ALTER TABLE `history` ADD INDEX `idx_history_batch_id`(`batch_id`);
ALTER TABLE `history` ADD INDEX `idx_history_message_id`(`message_id`);
ALTER TABLE `history` ADD INDEX `idx_history_delivery_id`(`delivery_id`);
