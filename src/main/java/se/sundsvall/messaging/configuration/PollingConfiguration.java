package se.sundsvall.messaging.configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import se.sundsvall.messaging.integration.EmailPoller;
import se.sundsvall.messaging.integration.SmsPoller;
import se.sundsvall.messaging.service.EmailService;
import se.sundsvall.messaging.service.SmsService;

@Configuration
class PollingConfiguration {

    private final Duration emailFixedDelay;
    private final Duration smsFixedDelay;

    PollingConfiguration(@Value("${polling.email.fixed-delay}") Duration emailFixedDelay,
                                @Value("${polling.sms.fixed-delay}") Duration smsFixedDelay) {
        this.emailFixedDelay = emailFixedDelay;
        this.smsFixedDelay = smsFixedDelay;
    }

    @Bean
    TaskScheduler pollingScheduler(EmailService emailService, SmsService smsService) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();

        scheduler.scheduleWithFixedDelay(new EmailPoller(emailService),emailFixedDelay.toMillis());
        scheduler.scheduleWithFixedDelay(new SmsPoller(smsService), smsFixedDelay.toMillis());

        return scheduler;
    }
}
