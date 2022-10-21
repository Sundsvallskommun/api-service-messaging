package se.sundsvall.messaging.integration.snailmailsender;

import generated.se.sundsvall.snailmail.SendSnailMailRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.messaging.dto.SnailmailDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SnailmailSenderIntegrationTests {

    @Mock
    private SnailmailSenderIntegrationMapper mockMapper;
    @Mock
    private SnailmailSenderClient mockClient;

    private SnailmailSenderIntegration integration;

    @BeforeEach
    void setUp() {
        integration = new SnailmailSenderIntegration(mockClient, mockMapper);
    }

    @Test
    void test_sendSnailmail() {
        when(mockMapper.toSendSnailmailRequest(any(SnailmailDto.class))).thenReturn(new SendSnailMailRequest());
        when(mockClient.sendSnailmail(any(SendSnailMailRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        integration.sendSnailmail(SnailmailDto.builder().build());

        verify(mockMapper, times(1)).toSendSnailmailRequest(any(SnailmailDto.class));
        verify(mockClient, times(1)).sendSnailmail(any(SendSnailMailRequest.class));
    }

    @Test
    void test_sendSnailmail_whenExceptionIsThrownByClient() {
        when(mockMapper.toSendSnailmailRequest(any(SnailmailDto.class))).thenReturn(new SendSnailMailRequest());
        when(mockClient.sendSnailmail(any(SendSnailMailRequest.class)))
                .thenThrow(Problem.builder()
                        .withStatus(Status.BAD_GATEWAY)
                        .withCause(Problem.builder()
                                .withStatus(Status.BAD_REQUEST)
                                .build())
                        .build());

        assertThatExceptionOfType(ThrowableProblem.class)
                .isThrownBy(() -> integration.sendSnailmail(SnailmailDto.builder().build()))
                .satisfies(problem -> {
                    assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
                    assertThat(problem.getCause()).isNotNull().satisfies(cause ->
                            assertThat(cause.getStatus()).isEqualTo(Status.BAD_REQUEST)
                    );
                });

        verify(mockMapper, times(1)).toSendSnailmailRequest(any(SnailmailDto.class));
        verify(mockClient, times(1)).sendSnailmail(any(SendSnailMailRequest.class));
    }
}
