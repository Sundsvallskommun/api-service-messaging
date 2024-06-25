package se.sundsvall.messaging.api;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.messaging.TestDataFactory.createValidWebMessageRequest;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.request.WebMessageRequest.Attachment;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;
import se.sundsvall.messaging.test.annotation.UnitTest;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@UnitTest
class MessageResourceWebMessageTest {

	private static final String URL = "/webmessage";
	private static final String ORIGIN_HEADER = "x-origin";
	private static final String ORIGIN = "origin";
	private static final InternalDeliveryResult DELIVERY_RESULT = InternalDeliveryResult.builder()
		.withMessageId("someMessageId")
		.withDeliveryId("someDeliveryId")
		.withMessageType(WEB_MESSAGE)
		.withStatus(SENT)
		.build();

	@MockBean
	private MessageService mockMessageService;

	@MockBean
	private MessageEventDispatcher mockEventDispatcher;

	@Autowired
	private WebTestClient webTestClient;

	@ParameterizedTest
	@MethodSource("argumentProvider")
	void sendSynchronous(String oepInstance, List<Attachment> attachments) {
		// Arrange
		final var request = createValidWebMessageRequest()
			.withAttachments(attachments)
			.withOepInstance(oepInstance);
		when(mockMessageService.sendWebMessage(any())).thenReturn(DELIVERY_RESULT);

		// Act
		final var response = webTestClient.post()
			.uri(URL)
			.header(ORIGIN_HEADER, ORIGIN)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectHeader().exists(LOCATION)
			.expectHeader().valuesMatch(LOCATION, "^/status/message/(.*)$")
			.expectStatus().isCreated()
			.expectBody(MessageResult.class)
			.returnResult()
			.getResponseBody();

		// Assert & verify
		assertThat(response).isNotNull();
		assertThat(response.messageId()).isEqualTo("someMessageId");
		assertThat(response.deliveries()).isNotNull().hasSize(1);
		assertThat(response.deliveries().getFirst().messageType()).isEqualTo(WEB_MESSAGE);
		assertThat(response.deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService).sendWebMessage(request.withOrigin(ORIGIN));
		verifyNoMoreInteractions(mockEventDispatcher);
		verifyNoInteractions(mockEventDispatcher);
	}

	@ParameterizedTest
	@MethodSource("argumentProvider")
	void sendAsynchronous(String oepInstance, List<Attachment> attachments) {
		// Arrange
		final var request = createValidWebMessageRequest()
			.withAttachments(attachments)
			.withOepInstance(oepInstance);
		when(mockEventDispatcher.handleWebMessageRequest(any())).thenReturn(DELIVERY_RESULT);

		// Act
		final var response = webTestClient.post()
			.uri(URL + "?async=true")
			.header(ORIGIN_HEADER, ORIGIN)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectHeader().exists(LOCATION)
			.expectHeader().valuesMatch(LOCATION, "^/status/message/(.*)$")
			.expectStatus().isCreated()
			.expectBody(MessageResult.class)
			.returnResult()
			.getResponseBody();

		// Assert & verify
		assertThat(response).isNotNull();
		assertThat(response.messageId()).isEqualTo("someMessageId");
		assertThat(response.deliveries()).isNotNull().hasSize(1);
		assertThat(response.deliveries().getFirst().messageType()).isEqualTo(WEB_MESSAGE);
		assertThat(response.deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockEventDispatcher).handleWebMessageRequest(request.withOrigin(ORIGIN));
		verifyNoMoreInteractions(mockEventDispatcher);
		verifyNoInteractions(mockMessageService);
	}

	private static Stream<Arguments> argumentProvider() {
		return Stream.of(
			Arguments.of(null, null),
			Arguments.of(null, emptyList()),
			Arguments.of("external", null),
			Arguments.of("external", emptyList()),
			Arguments.of("internal", null),
			Arguments.of("internal", emptyList()),
			Arguments.of("eXtErNaL", null),
			Arguments.of("eXtErNaL", emptyList()),
			Arguments.of("interNaL", null),
			Arguments.of("interNaL", emptyList()));
	}
}
