package se.sundsvall.messaging.integration.smssender;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.messaging.dto.SmsDto;

@ExtendWith(MockitoExtension.class)
class SmsSenderIntegrationTests {

    @Mock
    private RestTemplate mockRestTemplate;
    @Mock
    private SmsSenderIntegrationMapper mockSmsSenderIntegrationMapper;

    private SmsSenderIntegration smsSenderIntegration;

    @BeforeEach
    void setUp() {
        smsSenderIntegration = new SmsSenderIntegration(mockSmsSenderIntegrationMapper, mockRestTemplate);
    }

    @Test
    void sendSms_givenValidSmsRequest_thenSentTrueAndResponseStatus200_OK() {
        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Boolean.class)))
            .thenReturn(ResponseEntity.ok(true));

        var response = smsSenderIntegration.sendSms(createSmsDto());

        assertThat(response.getBody()).isTrue();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void sendSms_whenHttpStatusCodeException_isThrown_thenThrowsProblem() {
        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Boolean.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> smsSenderIntegration.sendSms(createSmsDto()))
            .satisfies(problem -> {
                assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
                assertThat(problem.getCause()).isNotNull();
                assertThat(problem.getCause().getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
            });
    }

    @Test
    void sendSms_whenRestClientException_isThrown_themThrowsProblem() {
        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Boolean.class)))
            .thenThrow(new RestClientException("dummy"));

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> smsSenderIntegration.sendSms(createSmsDto()))
            .satisfies(problem -> {
                assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
                assertThat(problem.getCause()).isNull();
            });
    }

    @Test
    void sendSms_whenUnableToSendSms_thenSentFalseAndResponseStatus200_OK() {
        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Boolean.class)))
            .thenReturn(ResponseEntity.ok(false));

        var response = smsSenderIntegration.sendSms(createSmsDto());

        assertThat(response.getBody()).isFalse();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private SmsDto createSmsDto() {
        return SmsDto.builder()
            .withMessage("message")
            .withMobileNumber("+46701234567")
            .withSender("sender")
            .build();
    }
}
