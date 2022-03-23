package se.sundsvall.messaging.integration.sms;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import generated.se.sundsvall.smssender.SmsRequest;

@ExtendWith(MockitoExtension.class)
class SmsIntegrationTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private SmsIntegrationProperties smsIntegrationProperties;
    private SmsIntegration smsIntegration;

    @BeforeEach
    void setUp() {
        smsIntegration = new SmsIntegration(restTemplate, smsIntegrationProperties);
    }

    @Test
    void sendSms_givenValidSmsRequest_thenSentTrueAndResponseStatus200_OK() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(true));

        ResponseEntity<Boolean> response = smsIntegration.sendSms(createSmsRequest());

        assertThat(response.getBody()).isTrue();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getMessageRetries_whenPropertySet_thenReturnsMessageRetriesProperty() {
        when(smsIntegrationProperties.getMessageRetries()).thenReturn(3);

        assertThat(smsIntegration.getMessageRetries()).isEqualTo(3);
    }

    @Test
    void sendSms_whenUnableToSendSms_thenSentFalseAndResponseStatus200_OK() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(false));

        ResponseEntity<Boolean> response = smsIntegration.sendSms(createSmsRequest());

        assertThat(response.getBody()).isFalse();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private SmsRequest createSmsRequest() {
        return new SmsRequest()
                .message("message")
                .mobileNumber("+46701234567")
                .sender("sender");
    }
}
