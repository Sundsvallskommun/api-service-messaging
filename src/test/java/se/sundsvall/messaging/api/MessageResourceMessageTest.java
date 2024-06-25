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
import static se.sundsvall.messaging.TestDataFactory.createValidMessageRequestMessage;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;
import se.sundsvall.messaging.test.annotation.UnitTest;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@UnitTest
class MessageResourceMessageTest {

	private static final String URL = "/messages";
	private static final String ORIGIN_HEADER = "x-origin";
	private static final String ORIGIN = "origin";
	private static final InternalDeliveryResult DELIVERY_RESULT = InternalDeliveryResult.builder()
		.withMessageId("someMessageId")
		.withDeliveryId("someDeliveryId")
		.withMessageType(MESSAGE)
		.withStatus(SENT)
		.build();
	private static final InternalDeliveryBatchResult DELIVERY_BATCH_RESULT = InternalDeliveryBatchResult.builder()
		.withBatchId("someBatchId")
		.withDeliveries(List.of(DELIVERY_RESULT))
		.build();

	@MockBean
	private MessageService mockMessageService;

	@MockBean
	private MessageEventDispatcher mockEventDispatcher;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void sendSynchronous() {
		// Arrange
		final var request = MessageRequest.builder().withMessages(List.of(createValidMessageRequestMessage())).build();
		when(mockMessageService.sendMessages(any())).thenReturn(DELIVERY_BATCH_RESULT);

		// Act
		final var response = webTestClient.post()
			.uri(URL)
			.header(ORIGIN_HEADER, ORIGIN)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectHeader().exists(LOCATION)
			.expectHeader().valuesMatch(LOCATION, "^/status/batch/(.*)$")
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

		verify(mockMessageService).sendMessages(request.withOrigin(ORIGIN));
		verifyNoMoreInteractions(mockEventDispatcher);
		verifyNoInteractions(mockEventDispatcher);
	}

	@Test
	void sendAsynchronous() {
		// Arrange
		final var request = MessageRequest.builder().withMessages(List.of(createValidMessageRequestMessage())).build();
		when(mockEventDispatcher.handleMessageRequest(any())).thenReturn(DELIVERY_BATCH_RESULT);

		// Act
		final var response = webTestClient.post()
			.uri(URL + "?async=true")
			.header(ORIGIN_HEADER, ORIGIN)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectHeader().exists(LOCATION)
			.expectHeader().valuesMatch(LOCATION, "^/status/batch/(.*)$")
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

		verify(mockEventDispatcher).handleMessageRequest(request.withOrigin(ORIGIN));
		verifyNoMoreInteractions(mockEventDispatcher);
		verifyNoInteractions(mockMessageService);
	}
}
