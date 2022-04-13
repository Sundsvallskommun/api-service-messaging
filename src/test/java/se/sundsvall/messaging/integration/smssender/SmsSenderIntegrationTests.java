package se.sundsvall.messaging.integration.smssender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.messaging.dto.SmsDto;

@ExtendWith(MockitoExtension.class)
class SmsSenderIntegrationTests {

    @Mock
    private SmsSenderIntegrationMapper mockMapper;
    @Mock
    private RestTemplate mockRestTemplate;

    private SmsSenderIntegration integration;

    @BeforeEach
    void setUp() {
        integration = new SmsSenderIntegration(mockMapper, mockRestTemplate);
    }

    @Test
    void test_sendSms() {
        integration.sendSms(SmsDto.builder().build());

        verify(mockMapper, times(1)).toSendSmsRequest(any(SmsDto.class));
        verify(mockRestTemplate, times(1)).postForEntity(any(String.class), any(HttpEntity.class), eq(Boolean.class));
    }

    @Test
    void test_sendSms_whenHttpStatusCodeExceptionIsThrownByRestTemplate() {
        when(mockRestTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Boolean.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> integration.sendSms(SmsDto.builder().build()))
            .satisfies(problem -> {
                assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
                assertThat(problem.getCause()).isNotNull().satisfies(cause ->
                        assertThat(cause.getStatus()).isEqualTo(Status.BAD_REQUEST)
                );
            });

        verify(mockMapper, times(1)).toSendSmsRequest(any(SmsDto.class));
        verify(mockRestTemplate, times(1)).postForEntity(any(String.class), any(HttpEntity.class), eq(Boolean.class));
    }

    @Test
    void test_sendSms_whenRestClientExceptionIsThrownByRestTemplate() {
        when(mockRestTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Boolean.class)))
            .thenThrow(new RestClientException("Unknown problem"));

        assertThatExceptionOfType(ThrowableProblem.class)
                .isThrownBy(() -> integration.sendSms(SmsDto.builder().build()))
                .satisfies(problem -> {
                    assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
                    assertThat(problem.getCause()).isNull();
                });

        verify(mockMapper, times(1)).toSendSmsRequest(any(SmsDto.class));
        verify(mockRestTemplate, times(1)).postForEntity(any(String.class), any(HttpEntity.class), eq(Boolean.class));
    }
}
