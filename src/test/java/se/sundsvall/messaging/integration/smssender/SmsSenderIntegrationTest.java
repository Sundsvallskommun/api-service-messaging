package se.sundsvall.messaging.integration.smssender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.messaging.test.annotation.UnitTest;

import generated.se.sundsvall.smssender.SendSmsRequest;
import generated.se.sundsvall.smssender.SendSmsResponse;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SmsSenderIntegrationTest {

    @Mock
    private SmsSenderIntegrationMapper mockMapper;
    @Mock
    private SmsSenderClient mockClient;

    private SmsSenderIntegration integration;

    @BeforeEach
    void setUp() {
        integration = new SmsSenderIntegration(mockClient, mockMapper);
    }

    @Test
    void test_sendSms() {
        when(mockMapper.toSendSmsRequest(any(SmsDto.class))).thenReturn(new SendSmsRequest());
        when(mockClient.sendSms(any(SendSmsRequest.class)))
            .thenReturn(ResponseEntity.ok(new SendSmsResponse().sent(true)));

        integration.sendSms(SmsDto.builder().build());

        verify(mockMapper, times(1)).toSendSmsRequest(any(SmsDto.class));
        verify(mockClient, times(1)).sendSms(any(SendSmsRequest.class));
    }

    @Test
    void test_sendSms_whenExceptionIsThrownByClient() {
        when(mockMapper.toSendSmsRequest(any(SmsDto.class))).thenReturn(new SendSmsRequest());
        when(mockClient.sendSms(any(SendSmsRequest.class)))
            .thenThrow(Problem.builder()
                .withStatus(Status.BAD_GATEWAY)
                .withCause(Problem.builder()
                    .withStatus(Status.BAD_REQUEST)
                    .build())
                .build());

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> integration.sendSms(SmsDto.builder().build()))
            .satisfies(problem -> {
                assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
                assertThat(problem.getCause()).isNotNull().satisfies(cause ->
                        assertThat(cause.getStatus()).isEqualTo(Status.BAD_REQUEST)
                );
            });

        verify(mockMapper, times(1)).toSendSmsRequest(any(SmsDto.class));
        verify(mockClient, times(1)).sendSms(any(SendSmsRequest.class));
    }
}
