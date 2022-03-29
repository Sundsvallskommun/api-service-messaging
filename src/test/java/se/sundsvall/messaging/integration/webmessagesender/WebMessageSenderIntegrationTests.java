package se.sundsvall.messaging.integration.webmessagesender;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

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

import se.sundsvall.messaging.dto.WebMessageDto;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.Party;

@ExtendWith(MockitoExtension.class)
class WebMessageSenderIntegrationTests {

    @Mock
    private RestTemplate mockRestTemplate;
    @Mock
    private WebMessageSenderIntegrationMapper mockWebMessageSenderIntegrationMapper;

    private WebMessageSenderIntegration webMessageSenderIntegrationSenderIntegration;

    @BeforeEach
    void setUp() {
        webMessageSenderIntegrationSenderIntegration = new WebMessageSenderIntegration(
            mockWebMessageSenderIntegrationMapper, mockRestTemplate);
    }

    @Test
    void sendWebMessage_givenValidCreateWebMessageRequest_thenSentTrueAndResponseStatus200_OK() {
        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
            .thenReturn(ResponseEntity.created(null).build());

        var response = webMessageSenderIntegrationSenderIntegration.sendWebMessage(createWebMessageDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void sendWebMessage_whenHttpStatusCodeException_isThrown_thenThrowsProblem() {
        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> webMessageSenderIntegrationSenderIntegration.sendWebMessage(createWebMessageDto()))
            .satisfies(problem -> {
                assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
                assertThat(problem.getCause()).isNotNull();
                assertThat(problem.getCause().getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
            });
    }

    @Test
    void sendWebMessage_whenRestClientException_isThrown_themThrowsProblem() {
        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
            .thenThrow(new RestClientException("dummy"));

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> webMessageSenderIntegrationSenderIntegration.sendWebMessage(createWebMessageDto()))
            .satisfies(problem -> {
                assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
                assertThat(problem.getCause()).isNull();
            });
    }

    @Test
    void sendWebMessage_whenUnableToSendWebMessage_thenSentFalseAndResponseStatus200_OK() {
        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
            .thenReturn(ResponseEntity.internalServerError().build());

        var response = webMessageSenderIntegrationSenderIntegration
            .sendWebMessage(createWebMessageDto());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private WebMessageDto createWebMessageDto() {
        return WebMessageDto.builder()
            .withMessage("someMessage")
            .withParty(Party.builder()
                .withPartyId("somePartyId")
                .withExternalReferences(List.of(
                    ExternalReference.builder()
                        .withKey("key")
                        .withValue("value")
                        .build()
                ))
                .build())
            .build();
    }
}
