package se.sundsvall.messaging.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sundsvall.messaging.service.EmailService;

public class EmailPoller implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(EmailPoller.class);

    private final EmailService emailService;

    public EmailPoller(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void run() {
        LOG.trace("Running E-mail poller");
        emailService.sendOldestPendingEmail();
    }
}
