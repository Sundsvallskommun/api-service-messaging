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
import static se.sundsvall.messaging.TestDataFactory.createExternalReference;
import static se.sundsvall.messaging.TestDataFactory.createValidWebMessageRequest;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest.Attachment;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;
import se.sundsvall.messaging.test.annotation.UnitTest;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@UnitTest
class MessageResourceWebMessageTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String URL = "/" + MUNICIPALITY_ID + "/webmessage";
	private static final String ORIGIN_HEADER = "x-origin";
	private static final String ORIGIN = "origin";
	private static final String ISSUER_HEADER = "x-issuer";
	private static final String ISSUER = "issuer";

	private static final InternalDeliveryResult DELIVERY_RESULT = InternalDeliveryResult.builder()
		.withMessageId("someMessageId")
		.withDeliveryId("someDeliveryId")
		.withMessageType(WEB_MESSAGE)
		.withMunicipalityId(MUNICIPALITY_ID)
		.withStatus(SENT)
		.build();

	@MockitoBean
	private MessageService mockMessageService;

	@MockitoBean
	private MessageEventDispatcher mockEventDispatcher;

	@Autowired
	private WebTestClient webTestClient;

	private static Stream<String> oepInstances() {
		return Stream.of(null, "external", "internal", "eXtErNaL", "interNaL");
	}

	private static Stream<List<Attachment>> attachments() {
		return Stream.of(null, emptyList());
	}

	private static Stream<Boolean> includeOptionalHeaders() {
		return Stream.of(true, false);
	}

	private static Stream<Boolean> withPartyId() {
		return Stream.of(true, false);
	}

	private static Stream<Arguments> argumentProvider() {
		// Create stream of all permutations
		return oepInstances()
			.flatMap(instance -> attachments()
				.flatMap(attachment -> includeOptionalHeaders()
					.flatMap(includeOptionalHeader -> withPartyId()
						.map(withPartyId -> Arguments.of(instance, attachment, includeOptionalHeader, withPartyId)))));
	}

	@ParameterizedTest
	@MethodSource("argumentProvider")
	void sendSynchronous(final String oepInstance, final List<Attachment> attachments, boolean includeOptionalHeaders, boolean withPartyId) {
		// Arrange
		final var request = createValidWebMessageRequest()
			.withAttachments(attachments)
			.withOepInstance(oepInstance)
			.withParty(WebMessageRequest.Party.builder()
				.withPartyId(withPartyId ? UUID.randomUUID().toString() : null)
				.withExternalReferences(List.of(createExternalReference()))
				.build());
		final var decoratedRequest = request.withMunicipalityId(MUNICIPALITY_ID);
		when(mockMessageService.sendWebMessage(any())).thenReturn(DELIVERY_RESULT);

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
		assertThat(response.deliveries().getFirst().messageType()).isEqualTo(WEB_MESSAGE);
		assertThat(response.deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService).sendWebMessage(includeOptionalHeaders ? addHeaderValues(decoratedRequest) : decoratedRequest);
		verifyNoMoreInteractions(mockEventDispatcher);
		verifyNoInteractions(mockEventDispatcher);
	}

	@ParameterizedTest
	@MethodSource("argumentProvider")
	void sendAsynchronous(final String oepInstance, final List<Attachment> attachments, boolean includeOptionalHeaders, boolean withPartyId) {
		// Arrange
		final var request = createValidWebMessageRequest()
			.withAttachments(attachments)
			.withOepInstance(oepInstance)
			.withParty(WebMessageRequest.Party.builder()
				.withPartyId(withPartyId ? UUID.randomUUID().toString() : null)
				.withExternalReferences(List.of(createExternalReference()))
				.build());
		final var decoratedRequest = request.withMunicipalityId(MUNICIPALITY_ID);
		when(mockEventDispatcher.handleWebMessageRequest(any())).thenReturn(DELIVERY_RESULT);

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
		assertThat(response.deliveries().getFirst().messageType()).isEqualTo(WEB_MESSAGE);
		assertThat(response.deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockEventDispatcher).handleWebMessageRequest(includeOptionalHeaders ? addHeaderValues(decoratedRequest) : decoratedRequest);
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

	private static WebMessageRequest addHeaderValues(WebMessageRequest request) {
		return request.withOrigin(ORIGIN)
			.withIssuer(ISSUER);
	}
}
