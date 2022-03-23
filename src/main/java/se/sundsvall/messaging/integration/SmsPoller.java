package se.sundsvall.messaging.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sundsvall.messaging.service.SmsService;

public class SmsPoller implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(SmsPoller.class);

    private final SmsService smsService;

    public SmsPoller(SmsService smsService) {
        this.smsService = smsService;
    }

    @Override
    public void run() {
        LOG.trace("Running SMS poller");
        smsService.sendOldestPendingSms();
    }
}
