package se.sundsvall.messaging.integration.emailsender;

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

import generated.se.sundsvall.emailsender.SendEmailRequest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class EmailSenderIntegrationTests {

    @Mock
    private EmailSenderIntegrationMapper mockMapper;
    @Mock
    private EmailSenderClient mockClient;

    private EmailSenderIntegration integration;

    @BeforeEach
    void setUp() {
        integration = new EmailSenderIntegration(mockClient, mockMapper);
    }

    @Test
    void test_sendEmail() {
        when(mockMapper.toSendEmailRequest(any(EmailDto.class))).thenReturn(new SendEmailRequest());
        when(mockClient.sendEmail(any(SendEmailRequest.class)))
            .thenReturn(ResponseEntity.ok().build());

        integration.sendEmail(EmailDto.builder().build());

        verify(mockMapper, times(1)).toSendEmailRequest(any(EmailDto.class));
        verify(mockClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void test_sendEmail_whenExceptionIsThrownByClient() {
        when(mockMapper.toSendEmailRequest(any(EmailDto.class))).thenReturn(new SendEmailRequest());
        when(mockClient.sendEmail(any(SendEmailRequest.class)))
            .thenThrow(Problem.builder()
                .withStatus(Status.BAD_GATEWAY)
                .withCause(Problem.builder()
                    .withStatus(Status.BAD_REQUEST)
                    .build())
                .build());

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> integration.sendEmail(EmailDto.builder().build()))
            .satisfies(problem -> {
                assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
                assertThat(problem.getCause()).isNotNull().satisfies(cause ->
                        assertThat(cause.getStatus()).isEqualTo(Status.BAD_REQUEST)
                );
            });

        verify(mockMapper, times(1)).toSendEmailRequest(any(EmailDto.class));
        verify(mockClient, times(1)).sendEmail(any(SendEmailRequest.class));
    }
}
