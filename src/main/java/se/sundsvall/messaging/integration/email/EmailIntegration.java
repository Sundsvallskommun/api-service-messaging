package se.sundsvall.messaging.integration.email;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import se.sundsvall.messaging.integration.AbstractRestIntegration;

import generated.se.sundsvall.emailsender.EmailRequest;

@Component
public class EmailIntegration extends AbstractRestIntegration {

    private final RestTemplate restTemplate;
    private final EmailIntegrationProperties emailProperties;

    public EmailIntegration(@Qualifier("integration.email-sender.resttemplate") RestTemplate restTemplate,
            EmailIntegrationProperties emailProperties) {
        this.restTemplate = restTemplate;
        this.emailProperties = emailProperties;
    }

    public int getMessageRetries() {
        return emailProperties.getMessageRetries();
    }

    public HttpStatus sendEmail(EmailRequest request) {
        return restTemplate.postForEntity("/send/email", new HttpEntity<>(request, createHeaders()), Void.class)
                .getStatusCode();
    }
}
