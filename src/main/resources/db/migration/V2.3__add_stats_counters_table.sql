CREATE TABLE `stats_counters` (
    `counter_name` VARCHAR(255) NOT NULL,
    `counter_value` INTEGER NOT NULL,
    PRIMARY KEY (`counter_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
