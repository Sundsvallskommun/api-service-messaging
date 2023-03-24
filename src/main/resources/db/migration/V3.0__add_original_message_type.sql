ALTER TABLE `messages` ADD COLUMN `original_message_type` varchar(16) DEFAULT NULL;
ALTER TABLE `history` ADD COLUMN `original_message_type` varchar(16) DEFAULT NULL;

UPDATE `history` SET `original_message_type`='LETTER', `message_type`='DIGITAL_MAIL' WHERE `message_type`='LETTER' AND `status`='SENT';
UPDATE `history` SET `original_message_type`='LETTER', `message_type`='SNAIL_MAIL' WHERE `message_type`='LETTER' AND `status`='FAILED';
UPDATE `history` SET `original_message_type`='MESSAGE' WHERE `message_type` = 'MESSAGE' AND `original_message_type` = NULL;
UPDATE `history` SET `original_message_type`=`message_type` WHERE `message_type` IS NULL;
