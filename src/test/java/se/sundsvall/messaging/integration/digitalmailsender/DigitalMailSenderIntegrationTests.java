package se.sundsvall.messaging.integration.digitalmailsender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.messaging.test.annotation.UnitTest;

import generated.se.sundsvall.digitalmailsender.DeliveryStatus;
import generated.se.sundsvall.digitalmailsender.DigitalMailRequest;
import generated.se.sundsvall.digitalmailsender.DigitalMailResponse;

@UnitTest
@ExtendWith(MockitoExtension.class)
class DigitalMailSenderIntegrationTests {

    @Mock
    private DigitalMailSenderClient mockClient;
    @Mock
    private DigitalMailSenderIntegrationMapper mockMapper;
    @Mock
    private ResponseEntity<DigitalMailResponse> mockResponseEntity;

    private DigitalMailSenderIntegration integration;

    @BeforeEach
    void setUp() {
        integration = new DigitalMailSenderIntegration(mockClient, mockMapper);
    }

    @Test
    void test_sendDigitalMail() {
        when(mockMapper.toDigitalMailRequest(any(DigitalMailDto.class)))
            .thenReturn(new DigitalMailRequest());
        when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockResponseEntity.getBody()).thenReturn(new DigitalMailResponse()
            .deliveryStatus(new DeliveryStatus().delivered(true)));
        when(mockClient.sendDigitalMail(any(DigitalMailRequest.class)))
            .thenReturn(mockResponseEntity);

        var response = integration.sendDigitalMail(createDigitalMailDto());
        assertThat(response).isEqualTo(SENT);

        verify(mockMapper, times(1)).toDigitalMailRequest(any(DigitalMailDto.class));
        verify(mockClient, times(1)).sendDigitalMail(any(DigitalMailRequest.class));
    }

    @Test
    void test_sendDigitalMail_whenExceptionIsThrownByClient() {
        when(mockMapper.toDigitalMailRequest(any(DigitalMailDto.class))).thenReturn(new DigitalMailRequest());
        when(mockClient.sendDigitalMail(any(DigitalMailRequest.class)))
            .thenThrow(Problem.builder()
                .withStatus(Status.BAD_GATEWAY)
                .withCause(Problem.builder()
                    .withStatus(Status.BAD_REQUEST)
                    .build())
                .build());

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> integration.sendDigitalMail(createDigitalMailDto()))
            .satisfies(problem -> {
                assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
                assertThat(problem.getCause()).isNotNull().satisfies(cause ->
                        assertThat(cause.getStatus()).isEqualTo(Status.BAD_REQUEST)
                );
            });

        verify(mockMapper, times(1)).toDigitalMailRequest(any(DigitalMailDto.class));
        verify(mockClient, times(1)).sendDigitalMail(any(DigitalMailRequest.class));
    }

    private DigitalMailDto createDigitalMailDto() {
        return DigitalMailDto.builder()
            .withPartyId("somePartyId")
            .build();
    }
}
