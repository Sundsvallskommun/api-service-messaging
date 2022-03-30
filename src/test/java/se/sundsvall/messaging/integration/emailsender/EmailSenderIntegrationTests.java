package se.sundsvall.messaging.integration.emailsender;

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

import se.sundsvall.messaging.dto.EmailDto;

@ExtendWith(MockitoExtension.class)
class EmailSenderIntegrationTests {

    @Mock
    private EmailSenderIntegrationMapper mockMapper;
    @Mock
    private RestTemplate mockRestTemplate;

    private EmailSenderIntegration integration;

    @BeforeEach
    void setUp() {
        integration = new EmailSenderIntegration(mockMapper, mockRestTemplate);
    }

    @Test
    void test_sendEmail() {
        integration.sendEmail(EmailDto.builder().build());

        verify(mockMapper, times(1)).toSendEmailRequest(any(EmailDto.class));
        verify(mockRestTemplate, times(1)).postForEntity(any(String.class), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void test_sendEmail_whenHttpStatusCodeExceptionIsThrownByRestTemplate() {
        when(mockRestTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> integration.sendEmail(EmailDto.builder().build()))
            .satisfies(problem -> {
                assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
                assertThat(problem.getCause()).isNotNull().satisfies(cause ->
                        assertThat(cause.getStatus()).isEqualTo(Status.BAD_REQUEST)
                );
            });

        verify(mockMapper, times(1)).toSendEmailRequest(any(EmailDto.class));
        verify(mockRestTemplate, times(1)).postForEntity(any(String.class), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void test_sendEmail_whenRestClientExceptionIsThrownByRestTemplate() {
        when(mockRestTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Void.class)))
            .thenThrow(new RestClientException("Unknown problem"));

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> integration.sendEmail(EmailDto.builder().build()))
            .satisfies(problem -> {
                assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
                assertThat(problem.getCause()).isNull();
            });

        verify(mockMapper, times(1)).toSendEmailRequest(any(EmailDto.class));
        verify(mockRestTemplate, times(1)).postForEntity(any(String.class), any(HttpEntity.class), eq(Void.class));
    }
}
