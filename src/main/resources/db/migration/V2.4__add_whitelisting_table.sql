CREATE TABLE `whitelisting` (
    `recipient` VARCHAR(255) NOT NULL,
    `message_type` VARCHAR(16) NOT NULL,
    PRIMARY KEY (`recipient`, `message_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
