CREATE TABLE `history` (
    `id` varchar(255) NOT NULL,
    `batch_id` varchar(255) DEFAULT NULL,
    `created_at` datetime(6) DEFAULT NULL,
    `message` longtext DEFAULT NULL,
    `message_id` varchar(255) DEFAULT NULL,
    `message_type` varchar(255) DEFAULT NULL,
    `party_contact` varchar(255) DEFAULT NULL,
    `party_id` varchar(255) DEFAULT NULL,
    `sender` varchar(255) DEFAULT NULL,
    `status` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `messages` (
    `message_id` varchar(255) NOT NULL,
    `batch_id` varchar(255) DEFAULT NULL,
    `email_name` varchar(255) DEFAULT NULL,
    `message` varchar(255) DEFAULT NULL,
    `message_status` varchar(255) DEFAULT NULL,
    `message_type` varchar(255) DEFAULT NULL,
    `party_id` varchar(255) DEFAULT NULL,
    `sender_email` varchar(255) DEFAULT NULL,
    `sms_name` varchar(255) DEFAULT NULL,
    `subject` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `queued_emails` (
    `message_id` varchar(255) NOT NULL,
    `batch_id` varchar(255) DEFAULT NULL,
    `created_at` datetime(6) DEFAULT NULL,
    `email_address` varchar(255) DEFAULT NULL,
    `html_message` longtext DEFAULT NULL,
    `message` varchar(4096) DEFAULT NULL,
    `party_id` varchar(255) DEFAULT NULL,
    `sender_email` varchar(255) DEFAULT NULL,
    `sender_name` varchar(255) DEFAULT NULL,
    `sending_attempts` int(11) NOT NULL,
    `status` varchar(255) DEFAULT NULL,
    `subject` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `queued_emails_attachments` (
    `id` varchar(255) NOT NULL,
    `content` varchar(255) DEFAULT NULL,
    `content_type` varchar(255) DEFAULT NULL,
    `name` varchar(255) DEFAULT NULL,
    `email_id` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FK7bvj9neuac5p8fyyub04a9lt8` (`email_id`),
    CONSTRAINT `FK7bvj9neuac5p8fyyub04a9lt8` FOREIGN KEY (`email_id`) REFERENCES `queued_emails` (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `queued_smses` (
    `message_id` varchar(255) NOT NULL,
    `batch_id` varchar(255) DEFAULT NULL,
    `created_at` datetime(6) DEFAULT NULL,
    `message` varchar(255) DEFAULT NULL,
    `mobile_number` varchar(255) DEFAULT NULL,
    `party_id` varchar(255) DEFAULT NULL,
    `sender` varchar(255) DEFAULT NULL,
    `sending_attempts` int(11) NOT NULL,
    `status` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
