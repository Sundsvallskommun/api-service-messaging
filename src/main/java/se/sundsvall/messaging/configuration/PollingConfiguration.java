package se.sundsvall.messaging.configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import se.sundsvall.messaging.service.EmailService;
import se.sundsvall.messaging.service.SmsService;

@Configuration
class PollingConfiguration {

    private final Duration emailFixedDelay;
    private final Duration smsFixedDelay;

    PollingConfiguration(@Value("${polling.email.fixed-delay}") final Duration emailFixedDelay,
            @Value("${polling.sms.fixed-delay}") final Duration smsFixedDelay) {
        this.emailFixedDelay = emailFixedDelay;
        this.smsFixedDelay = smsFixedDelay;
    }

    @Bean
    TaskScheduler pollingScheduler(final EmailService emailService, final SmsService smsService) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();

        scheduler.scheduleWithFixedDelay(emailService,emailFixedDelay.toMillis());
        scheduler.scheduleWithFixedDelay(smsService, smsFixedDelay.toMillis());

        return scheduler;
    }
}
