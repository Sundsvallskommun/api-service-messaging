package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailRequest;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.EMAIL;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.request.EmailRequest.Party;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;
import se.sundsvall.messaging.test.annotation.UnitTest;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@UnitTest
class MessageResourceEmailTest {

	private static final String URL = "/email";
	private static final String ORIGIN_HEADER = "x-origin";
	private static final String ORIGIN = "origin";
	private static final InternalDeliveryResult DELIVERY_RESULT = InternalDeliveryResult.builder()
		.withMessageId("someMessageId")
		.withDeliveryId("someDeliveryId")
		.withMessageType(EMAIL)
		.withStatus(SENT)
		.build();

	@MockBean
	private MessageService mockMessageService;

	@MockBean
	private MessageEventDispatcher mockEventDispatcher;

	@Autowired
	private WebTestClient webTestClient;

	@ParameterizedTest
	@MethodSource("requestProvider")
	void sendSynchronous(String replyTo, Party party) {
		// Arrange
		final var request = createValidEmailRequest().withParty(party);
		request.sender().withReplyTo(replyTo);
		when(mockMessageService.sendEmail(any())).thenReturn(DELIVERY_RESULT);

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
		assertThat(response.deliveries().getFirst().messageType()).isEqualTo(EMAIL);
		assertThat(response.deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService).sendEmail(request.withOrigin(ORIGIN));
		verifyNoMoreInteractions(mockEventDispatcher);
		verifyNoInteractions(mockEventDispatcher);
	}

	@ParameterizedTest
	@MethodSource("requestProvider")
	void sendAsynchronous(String replyTo, Party party) {
		// Arrange
		final var request = createValidEmailRequest().withParty(party);
		request.sender().withReplyTo(replyTo);
		when(mockEventDispatcher.handleEmailRequest(any())).thenReturn(DELIVERY_RESULT);

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
		assertThat(response.deliveries().getFirst().messageType()).isEqualTo(EMAIL);
		assertThat(response.deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockEventDispatcher).handleEmailRequest(request.withOrigin(ORIGIN));
		verifyNoMoreInteractions(mockEventDispatcher);
		verifyNoInteractions(mockMessageService);
	}

	private static Stream<Arguments> requestProvider() {
		final var validRequest = createValidEmailRequest();

		return Stream.of(Arguments.of("sender@sender.se", validRequest.party()),
			Arguments.of("sender@sender.se", null),
			Arguments.of(null, null));
	}
}
