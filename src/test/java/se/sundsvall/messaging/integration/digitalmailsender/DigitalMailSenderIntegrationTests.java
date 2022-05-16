package se.sundsvall.messaging.integration.digitalmailsender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.messaging.dto.DigitalMailDto;

import generated.se.sundsvall.digitalmailsender.DeliveryStatus;
import generated.se.sundsvall.digitalmailsender.SecureDigitalMailResponse;

@ExtendWith(MockitoExtension.class)
class DigitalMailSenderIntegrationTests {

    @Mock
    private DigitalMailSenderIntegrationMapper mockMapper;
    @Mock
    private RestTemplate mockRestTemplate;

    @Mock
    private ResponseEntity<SecureDigitalMailResponse> mockResponseEntity;

    private DigitalMailSenderIntegration integration;

    @BeforeEach
    void setUp() {
        integration = new DigitalMailSenderIntegration(mockMapper, mockRestTemplate);
    }

    @Test
    void test_sendDigitalMail() {
        when(mockResponseEntity.getBody()).thenReturn(new SecureDigitalMailResponse()
            .deliveryStatus(new DeliveryStatus().delivered(true)));
        when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockRestTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(SecureDigitalMailResponse.class), anyMap()))
            .thenReturn(mockResponseEntity);

        var response = integration.sendDigitalMail(createDigitalMailDto());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().booleanValue()).isTrue();

        verify(mockMapper, times(1)).toDigitalMailRequest(any(DigitalMailDto.class));
        verify(mockRestTemplate, times(1)).postForEntity(any(String.class), any(HttpEntity.class), eq(SecureDigitalMailResponse.class), anyMap());
    }

    @Test
    void test_sendDigitalMail_whenHttpStatusCodeExceptionIsThrownByRestTemplate() {
        when(mockRestTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(SecureDigitalMailResponse.class), anyMap()))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> integration.sendDigitalMail(createDigitalMailDto()))
            .satisfies(problem -> {
                assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
                assertThat(problem.getCause()).isNotNull().satisfies(cause ->
                        assertThat(cause.getStatus()).isEqualTo(Status.BAD_REQUEST)
                );
            });

        verify(mockMapper, times(1)).toDigitalMailRequest(any(DigitalMailDto.class));
        verify(mockRestTemplate, times(1)).postForEntity(any(String.class), any(HttpEntity.class), eq(SecureDigitalMailResponse.class), anyMap());
    }

    @Test
    void test_sendDigitalMail_whenRestClientExceptionIsThrownByRestTemplate() {
        when(mockRestTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(SecureDigitalMailResponse.class), anyMap()))
            .thenThrow(new RestClientException("Unknown problem"));

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> integration.sendDigitalMail(createDigitalMailDto()))
            .satisfies(problem -> {
                assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
                assertThat(problem.getCause()).isNull();
            });

        verify(mockMapper, times(1)).toDigitalMailRequest(any(DigitalMailDto.class));
        verify(mockRestTemplate, times(1)).postForEntity(any(String.class), any(HttpEntity.class), eq(SecureDigitalMailResponse.class), anyMap());
    }

    private DigitalMailDto createDigitalMailDto() {
        return DigitalMailDto.builder()
            .withPartyId("somePartyId")
            .build();
    }
}
