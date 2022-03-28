package se.sundsvall.messaging.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import se.sundsvall.messaging.service.EmailService;
import se.sundsvall.messaging.service.SmsService;
import se.sundsvall.messaging.service.WebMessageService;

@Configuration
class PollingConfiguration {

    @Bean
    TaskScheduler pollingScheduler(final EmailService emailService, final SmsService smsService,
            final WebMessageService webMessageService) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();

        scheduler.scheduleWithFixedDelay(emailService, emailService.getPollDelay());
        scheduler.scheduleWithFixedDelay(smsService, smsService.getPollDelay());
        scheduler.scheduleWithFixedDelay(webMessageService, webMessageService.getPollDelay());

        return scheduler;
    }
}
