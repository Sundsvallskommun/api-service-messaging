package se.sundsvall.messaging.integration.oepintegrator;

import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.MessageStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OepIntegratorIntegrationTest {

	@Mock
	private OepIntegratorClient client;

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private OepIntegratorMapper mapper;

	@InjectMocks
	private OepIntegratorIntegration integration;

	@ParameterizedTest
	@MethodSource("argumentsProvider")
	void sendWebMessage(final HttpStatus status, final MessageStatus expected) {
		final var municipalityId = "2281";
		final var partyId = "828ab8cf-c07e-47ff-b5e3-82dea276b2e4";
		final var externalReference = new ExternalReference("key", "value");
		final var externalReferences = List.of(externalReference);
		final var message = "message";
		final var userId = "userId";
		final var oepInstance = "oepInstance";
		final var webMessageDto = new WebMessageDto(partyId, externalReferences, message, userId, oepInstance, false, null);
		final var attachments = List.of(new WebMessageRequest.Attachment("fileName", "mimeType", createBase64String()));
		final var responseMock = mock(ResponseEntity.class);

		doReturn(responseMock).when(client).createWebmessage(eq(municipalityId), eq(webMessageDto.oepInstance()), any(), any());
		when(responseMock.getStatusCode()).thenReturn(status);

		final var result = integration.sendWebMessage(municipalityId, webMessageDto, attachments);

		assertThat(result).isNotNull().isEqualTo(expected);
	}

	private static Stream<Arguments> argumentsProvider() {
		return Stream.of(
			Arguments.of(HttpStatus.OK, MessageStatus.SENT),
			Arguments.of(HttpStatus.NOT_FOUND, MessageStatus.NOT_SENT));
	}

	private String createBase64String() {
		return Base64.getEncoder().encodeToString("test".getBytes());
	}
}
