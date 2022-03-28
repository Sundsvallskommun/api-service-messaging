package se.sundsvall.messaging.integration.emailsender;

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

import se.sundsvall.messaging.dto.EmailDto;

@ExtendWith(MockitoExtension.class)
class EmailIntegrationTests {

    @Mock
    private RestTemplate mockRestTemplate;
    @Mock
    private EmailSenderIntegrationProperties mockEmailIntegrationProperties;
    @Mock
    private EmailSenderIntegrationMapper mockEmailIntegrationMapper;

    private EmailSenderIntegration emailSenderIntegration;

    @BeforeEach
    void setUp() {
        emailSenderIntegration = new EmailSenderIntegration(mockRestTemplate,
            mockEmailIntegrationMapper, mockEmailIntegrationProperties);
    }

    @Test
    void sendEmail_givenValidEmailRequest_thenResponseStatus200_OK() {
        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
            .thenReturn(ResponseEntity.ok(null));

        var response = emailSenderIntegration.sendEmail(validRequest());

        assertThat(response).isEqualTo(HttpStatus.OK);
    }

    private EmailDto validRequest() {
        return EmailDto.builder()
            .withEmailAddress("reciver@email.com")
            .withSubject("subject")
            .withMessage("message")
            .withHtmlMessage("base64 html message")
            .withSenderName("senderName")
            .withSenderEmail("sender@email.com")
            .withAttachments(Collections.emptyList())
            .build();
    }
}
