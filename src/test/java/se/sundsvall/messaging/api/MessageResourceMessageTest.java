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
import static se.sundsvall.messaging.TestDataFactory.MUNICIPALITY_ID;
import static se.sundsvall.messaging.TestDataFactory.X_ISSUER_HEADER;
import static se.sundsvall.messaging.TestDataFactory.X_ISSUER_HEADER_VALUE;
import static se.sundsvall.messaging.TestDataFactory.X_ORIGIN_HEADER;
import static se.sundsvall.messaging.TestDataFactory.X_ORIGIN_HEADER_VALUE;
import static se.sundsvall.messaging.TestDataFactory.X_SENT_BY_HEADER;
import static se.sundsvall.messaging.TestDataFactory.X_SENT_BY_HEADER_USER_NAME;
import static se.sundsvall.messaging.TestDataFactory.X_SENT_BY_HEADER_VALUE;
import static se.sundsvall.messaging.TestDataFactory.createValidMessageRequestMessage;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;

import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class MessageResourceMessageTest {

	private static final String URL = "/" + MUNICIPALITY_ID + "/messages";

	private static final InternalDeliveryResult DELIVERY_RESULT = InternalDeliveryResult.builder()
		.withMessageId("someMessageId")
		.withDeliveryId("someDeliveryId")
		.withMunicipalityId(MUNICIPALITY_ID)
		.withMessageType(MESSAGE)
		.withStatus(SENT)
		.build();

	private static final InternalDeliveryBatchResult DELIVERY_BATCH_RESULT = InternalDeliveryBatchResult.builder()
		.withBatchId("someBatchId")
		.withMunicipalityId(MUNICIPALITY_ID)
		.withDeliveries(List.of(DELIVERY_RESULT))
		.build();

	@MockitoBean
	private MessageService mockMessageService;

	@MockitoBean
	private MessageEventDispatcher mockEventDispatcher;

	@Autowired
	private WebTestClient webTestClient;

	@ParameterizedTest
	@ValueSource(booleans = {
		true, false
	})
	void sendSynchronous(boolean includeOptionalHeaders) {
		// Arrange
		final var request = MessageRequest.builder().withMessages(List.of(createValidMessageRequestMessage())).build();
		final var decoratedRequest = request.withMunicipalityId(MUNICIPALITY_ID);
		when(mockMessageService.sendMessages(any())).thenReturn(DELIVERY_BATCH_RESULT);

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
		assertThat(response.messages()).hasSize(1).allSatisfy(messageResult -> {
			assertThat(messageResult.messageId()).isEqualTo("someMessageId");
			assertThat(messageResult.deliveries()).isNotNull().hasSize(1);
			assertThat(messageResult.deliveries().getFirst().messageType()).isEqualTo(MESSAGE);
			assertThat(messageResult.deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
			assertThat(messageResult.deliveries().getFirst().status()).isEqualTo(SENT);
		});

		verify(mockMessageService).sendMessages(includeOptionalHeaders ? addHeaderValues(decoratedRequest) : decoratedRequest);
		verifyNoMoreInteractions(mockEventDispatcher);
		verifyNoInteractions(mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(booleans = {
		true, false
	})
	void sendAsynchronous(boolean includeOptionalHeaders) {
		// Arrange
		final var request = MessageRequest.builder().withMessages(List.of(createValidMessageRequestMessage())).build();
		final var decoratedRequest = request.withMunicipalityId(MUNICIPALITY_ID);
		when(mockEventDispatcher.handleMessageRequest(any())).thenReturn(DELIVERY_BATCH_RESULT);

		// Act
		final var response = webTestClient.post()
			.uri(URL + "?async=true")
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
		assertThat(response.messages()).hasSize(1).allSatisfy(messageResult -> {
			assertThat(messageResult.messageId()).isEqualTo("someMessageId");
			assertThat(messageResult.deliveries()).isNotNull().hasSize(1);
			assertThat(messageResult.deliveries().getFirst().messageType()).isEqualTo(MESSAGE);
			assertThat(messageResult.deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
			assertThat(messageResult.deliveries().getFirst().status()).isEqualTo(SENT);
		});

		verify(mockEventDispatcher).handleMessageRequest(includeOptionalHeaders ? addHeaderValues(decoratedRequest) : decoratedRequest);
		verifyNoMoreInteractions(mockEventDispatcher);
		verifyNoInteractions(mockMessageService);
	}

	@Test
	void testOldHeadersShouldBePreserved() {
		// Arrange
		final var request = MessageRequest.builder().withMessages(List.of(createValidMessageRequestMessage())).build();
		final var decoratedRequest = request.withMunicipalityId(MUNICIPALITY_ID);
		when(mockMessageService.sendMessages(any())).thenReturn(DELIVERY_BATCH_RESULT);

		// Act
		final var response = webTestClient.post()
			.uri(URL)
			.headers(oldHeaders())
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
		assertThat(response.messages()).hasSize(1).allSatisfy(messageResult -> {
			assertThat(messageResult.messageId()).isEqualTo("someMessageId");
			assertThat(messageResult.deliveries()).isNotNull().hasSize(1);
			assertThat(messageResult.deliveries().getFirst().messageType()).isEqualTo(MESSAGE);
			assertThat(messageResult.deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
			assertThat(messageResult.deliveries().getFirst().status()).isEqualTo(SENT);
		});

		verify(mockMessageService).sendMessages(decoratedRequest.withOrigin(X_ORIGIN_HEADER_VALUE).withIssuer(X_ISSUER_HEADER_VALUE));
		verifyNoMoreInteractions(mockEventDispatcher);
		verifyNoInteractions(mockEventDispatcher);
	}

	private static Consumer<HttpHeaders> handleHeaders(boolean includeOptionalHeaders) {
		return httpHeaders -> {
			if (includeOptionalHeaders) {
				httpHeaders.add(X_ORIGIN_HEADER, X_ORIGIN_HEADER_VALUE);
				httpHeaders.add(X_ISSUER_HEADER, X_ISSUER_HEADER_VALUE);
				httpHeaders.add(X_SENT_BY_HEADER, X_SENT_BY_HEADER_VALUE);
			}
		};
	}

	private static Consumer<HttpHeaders> oldHeaders() {
		return httpHeaders -> {
			httpHeaders.add(X_ORIGIN_HEADER, X_ORIGIN_HEADER_VALUE);
			httpHeaders.add(X_ISSUER_HEADER, X_ISSUER_HEADER_VALUE);
		};
	}

	private static MessageRequest addHeaderValues(MessageRequest request) {
		return request.withOrigin(X_ORIGIN_HEADER_VALUE)
			.withIssuer(X_SENT_BY_HEADER_USER_NAME);
	}
}
