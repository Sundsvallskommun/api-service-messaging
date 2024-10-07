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
import static se.sundsvall.messaging.TestDataFactory.createValidSmsBatchRequest;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.SMS;

import java.util.List;
import java.util.UUID;
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
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;
import se.sundsvall.messaging.test.annotation.UnitTest;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@UnitTest
class MessageResourceSmsBatchTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String URL = "/" + MUNICIPALITY_ID + "/sms/batch";
	private static final String ORIGIN_HEADER = "x-origin";
	private static final String ORIGIN = "origin";
	private static final String ORIGIN_ISSUER = "x-issuer";
	private static final String ISSUER = "issuer";

	private static final InternalDeliveryResult DELIVERY_RESULT = InternalDeliveryResult.builder()
		.withMessageId("someMessageId")
		.withDeliveryId("someDeliveryId")
		.withMessageType(SMS)
		.withMunicipalityId(MUNICIPALITY_ID)
		.withStatus(SENT)
		.build();

	private static final InternalDeliveryBatchResult DELIVERY_BATCH_RESULT = InternalDeliveryBatchResult.builder()
		.withBatchId("someBatchId")
		.withMunicipalityId(MUNICIPALITY_ID)
		.withDeliveries(List.of(DELIVERY_RESULT))
		.build();

	@MockBean
	private MessageService mockMessageService;

	@MockBean
	private MessageEventDispatcher mockEventDispatcher;

	@Autowired
	private WebTestClient webTestClient;

	private static Stream<Arguments> requestProvider() {
		return Stream.of(
			Arguments.of("abc", UUID.randomUUID().toString(), true),
			Arguments.of("abc", UUID.randomUUID().toString(), false),
			Arguments.of("abc12", UUID.randomUUID().toString(), true),
			Arguments.of("abc12", UUID.randomUUID().toString(), false),
			Arguments.of("Min Bankman", UUID.randomUUID().toString(), true),
			Arguments.of("Min Bankman", UUID.randomUUID().toString(), false),
			Arguments.of(null, UUID.randomUUID().toString(), true),
			Arguments.of(null, UUID.randomUUID().toString(), false),
			Arguments.of("abc", null, true),
			Arguments.of("abc", null, false),
			Arguments.of("abc12", null, true),
			Arguments.of("abc12", null, false),
			Arguments.of("Min Bankman", null, true),
			Arguments.of("Min Bankman", null, false),
			Arguments.of(null, null, true),
			Arguments.of(null, null, false));
	}

	@ParameterizedTest
	@MethodSource("requestProvider")
	void sendBatch(final String senderName, final String partyId, boolean includeOptionalHeaders) {
		// Arrange
		var request = createValidSmsBatchRequest();
		request = request.withSender(senderName).withParties(List.of(request.parties().getFirst().withPartyId(partyId)));
		when(mockEventDispatcher.handleSmsBatchRequest(any(), any())).thenReturn(DELIVERY_BATCH_RESULT);

		// Act
		final var response = webTestClient.post()
			.uri(URL)
			.headers(handleHeaders(includeOptionalHeaders))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectHeader().exists(LOCATION)
			.expectHeader().valuesMatch(LOCATION, "^/" + MUNICIPALITY_ID + "/status/batch/(.*)$")
			.expectStatus().isCreated()
			.expectBody(MessageBatchResult.class)
			.returnResult()
			.getResponseBody();

		// Assert & verify
		assertThat(response).isNotNull();
		assertThat(response.batchId()).isEqualTo("someBatchId");
		assertThat(response.messages()).isNotNull().hasSize(1);
		assertThat(response.messages().getFirst().messageId()).isEqualTo("someMessageId");
		assertThat(response.messages().getFirst().deliveries()).isNotNull().hasSize(1);
		assertThat(response.messages().getFirst().deliveries().getFirst().messageType()).isEqualTo(SMS);
		assertThat(response.messages().getFirst().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.messages().getFirst().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockEventDispatcher).handleSmsBatchRequest(includeOptionalHeaders ? request.withOrigin(ORIGIN) : request, MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockEventDispatcher);
		verifyNoInteractions(mockMessageService);
	}

	private static Consumer<HttpHeaders> handleHeaders(boolean includeOptionalHeaders) {
		return httpHeaders -> {
			if (includeOptionalHeaders) {
				httpHeaders.add(ORIGIN_HEADER, ORIGIN);
				httpHeaders.add(ORIGIN_ISSUER, ISSUER);
			}
		};
	}
}
