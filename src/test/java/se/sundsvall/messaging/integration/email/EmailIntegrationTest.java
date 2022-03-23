package se.sundsvall.messaging.integration.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import generated.se.sundsvall.emailsender.EmailRequest;

@ExtendWith(MockitoExtension.class)
class EmailIntegrationTest {

    @Mock
    private RestTemplate restTemplate;
    private EmailIntegration emailIntegration;
    @Mock
    private EmailIntegrationProperties emailProperties;

    @BeforeEach
    void setUp() {
        emailIntegration = new EmailIntegration(restTemplate, emailProperties);
    }

    @Test
    void sendEmail_givenValidEmailRequest_thenResponseStatus200_OK() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok(null));

        HttpStatus response = emailIntegration.sendEmail(validRequest());

        assertThat(response).isEqualTo(HttpStatus.OK);
    }

    private EmailRequest validRequest() {
        return new EmailRequest()
                .emailAddress("reciver@email.com")
                .subject("subject")
                .message("message")
                .htmlMessage("base64 html message")
                .senderName("senderName")
                .senderEmail("sender@email.com")
                .attachments(Collections.emptyList());
    }
}
