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

import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest.Party;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;
import se.sundsvall.messaging.test.annotation.UnitTest;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@UnitTest
class MessageResourceEmailTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String URL = "/" + MUNICIPALITY_ID + "/email";
	private static final String ORIGIN_HEADER = "x-origin";
	private static final String ORIGIN = "origin";
	private static final String ISSUER_HEADER = "x-issuer";
	private static final String ISSUER = "issuer";

	private static final InternalDeliveryResult DELIVERY_RESULT = InternalDeliveryResult.builder()
		.withMessageId("someMessageId")
		.withDeliveryId("someDeliveryId")
		.withMunicipalityId(MUNICIPALITY_ID)
		.withMessageType(EMAIL)
		.withStatus(SENT)
		.build();

	@MockBean
	private MessageService mockMessageService;

	@MockBean
	private MessageEventDispatcher mockEventDispatcher;

	@Autowired
	private WebTestClient webTestClient;

	private static Stream<Arguments> requestProvider() {
		final var validRequest = createValidEmailRequest();

		return Stream.of(
			Arguments.of("sender@sender.se", validRequest.party(), true),
			Arguments.of("sender@sender.se", validRequest.party(), false),
			Arguments.of("sender@sender.se", null, true),
			Arguments.of("sender@sender.se", null, false),
			Arguments.of(null, null, true),
			Arguments.of(null, null, false));
	}

	@ParameterizedTest
	@MethodSource("requestProvider")
	void sendSynchronous(String replyTo, Party party, boolean includeOptionalHeaders) {
		// Arrange
		var request = createValidEmailRequest();
		request = request.withParty(party).withSender(request.sender().withReplyTo(replyTo));
		final var decoratedRequest = request.withMunicipalityId(MUNICIPALITY_ID);
		when(mockMessageService.sendEmail(any())).thenReturn(DELIVERY_RESULT);

		// Act
		final var response = webTestClient.post()
			.uri(URL)
			.headers(handleHeaders(includeOptionalHeaders))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectHeader().exists(LOCATION)
			.expectHeader().valuesMatch(LOCATION, "^/" + MUNICIPALITY_ID + "/status/message/(.*)$")
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

		verify(mockMessageService).sendEmail(includeOptionalHeaders ? addHeaderValues(decoratedRequest) : decoratedRequest);
		verifyNoMoreInteractions(mockEventDispatcher);
		verifyNoInteractions(mockEventDispatcher);
	}

	@ParameterizedTest
	@MethodSource("requestProvider")
	void sendAsynchronous(String replyTo, Party party, boolean includeOptionalHeaders) {
		// Arrange
		var request = createValidEmailRequest();
		request = request.withParty(party).withSender(request.sender().withReplyTo(replyTo));
		final var decoratedRequest = request.withMunicipalityId(MUNICIPALITY_ID);

		when(mockEventDispatcher.handleEmailRequest(any())).thenReturn(DELIVERY_RESULT);

		// Act
		final var response = webTestClient.post()
			.uri(URL + "?async=true")
			.headers(handleHeaders(includeOptionalHeaders))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectHeader().exists(LOCATION)
			.expectHeader().valuesMatch(LOCATION, "^/" + MUNICIPALITY_ID + "/status/message/(.*)$")
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

		verify(mockEventDispatcher).handleEmailRequest(includeOptionalHeaders ? addHeaderValues(decoratedRequest) : decoratedRequest);
		verifyNoMoreInteractions(mockEventDispatcher);
		verifyNoInteractions(mockMessageService);
	}

	private static Consumer<HttpHeaders> handleHeaders(boolean includeOptionalHeaders) {
		return httpHeaders -> {
			if (includeOptionalHeaders) {
				httpHeaders.add(ORIGIN_HEADER, ORIGIN);
				httpHeaders.add(ISSUER_HEADER, ISSUER);
			}
		};
	}

	private static EmailRequest addHeaderValues(EmailRequest request) {
		return request.withOrigin(ORIGIN)
			.withIssuer(ISSUER);
	}
}
