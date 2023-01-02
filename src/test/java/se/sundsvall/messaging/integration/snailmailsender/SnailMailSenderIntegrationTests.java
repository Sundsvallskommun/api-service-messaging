package se.sundsvall.messaging.integration.snailmailsender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.messaging.test.annotation.UnitTest;

import generated.se.sundsvall.snailmail.SendSnailMailRequest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SnailMailSenderIntegrationTests {

    @Mock
    private SnailMailSenderIntegrationMapper mockMapper;
    @Mock
    private SnailMailSenderClient mockClient;

    @InjectMocks
    private SnailMailSenderIntegration integration;

    @Test
    void test_sendSnailmail() {
        when(mockMapper.toSendSnailmailRequest(any(SnailMailDto.class))).thenReturn(new SendSnailMailRequest());
        when(mockClient.sendSnailmail(any(SendSnailMailRequest.class)))
            .thenReturn(ResponseEntity.ok().build());

        integration.sendSnailmail(SnailMailDto.builder().build());

        verify(mockMapper, times(1)).toSendSnailmailRequest(any(SnailMailDto.class));
        verify(mockClient, times(1)).sendSnailmail(any(SendSnailMailRequest.class));
    }

    @Test
    void test_sendSnailmail_whenExceptionIsThrownByClient() {
        when(mockMapper.toSendSnailmailRequest(any(SnailMailDto.class))).thenReturn(new SendSnailMailRequest());
        when(mockClient.sendSnailmail(any(SendSnailMailRequest.class)))
            .thenThrow(Problem.builder()
                .withStatus(Status.BAD_GATEWAY)
                .withCause(Problem.builder()
                    .withStatus(Status.BAD_REQUEST)
                    .build())
                .build());

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> integration.sendSnailmail(SnailMailDto.builder().build()))
            .satisfies(problem -> {
                assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
                assertThat(problem.getCause()).isNotNull().satisfies(cause ->
                        assertThat(cause.getStatus()).isEqualTo(Status.BAD_REQUEST)
                );
            });

        verify(mockMapper, times(1)).toSendSnailmailRequest(any(SnailMailDto.class));
        verify(mockClient, times(1)).sendSnailmail(any(SendSnailMailRequest.class));
    }
}
